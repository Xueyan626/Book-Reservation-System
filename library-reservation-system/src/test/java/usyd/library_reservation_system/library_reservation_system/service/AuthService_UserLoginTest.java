package usyd.library_reservation_system.library_reservation_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import usyd.library_reservation_system.library_reservation_system.dto.LoginStartRequest;
import usyd.library_reservation_system.library_reservation_system.dto.LoginStartResponse;
import usyd.library_reservation_system.library_reservation_system.dto.LoginSuccessResponse;
import usyd.library_reservation_system.library_reservation_system.dto.LoginVerifyRequest;
import usyd.library_reservation_system.library_reservation_system.repository.UserRepository;
import usyd.library_reservation_system.library_reservation_system.model.UserEntity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * High-confidence unit tests for AuthService_UserLogin.
 * - All dependencies are mocked.
 * - No reliance on real time, randomness, or external systems.
 * - Verifies key side effects and token claim contents.
 */
class AuthService_UserLoginTest {

    private UserRepository userRepo;
    private PasswordEncoder passwordEncoder;
    private EmailService emailService;
    private TokenService tokenService;

    private AuthService_UserLogin service;

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        emailService = mock(EmailService.class);
        tokenService = mock(TokenService.class);

        service = new AuthService_UserLogin(userRepo, passwordEncoder, emailService, tokenService);

        // Inject @Value fields to deterministic values for tests
        ReflectionTestUtils.setField(service, "codeTtlMinutes", 10);
        ReflectionTestUtils.setField(service, "codeLength", 6);
    }

    // ===== startLogin() tests =====

    @Test
    @DisplayName("startLogin: user does NOT exist -> still returns token, no email is sent, codeHash=NA and no userId in claims")
    void startLogin_userNotFound() {
        when(userRepo.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.empty());
        when(tokenService.sign(anyMap(), any(Instant.class))).thenReturn("JWT-FAKE");

        LoginStartResponse resp = service.startLogin(new LoginStartRequest("Alice@Example.com"));

        // Response assertions
        assertNotNull(resp);
        assertEquals("login code sent", resp.message());
        assertEquals("JWT-FAKE", resp.challengeToken());

        // Verify we did NOT send an email
        verify(emailService, never()).sendLoginCode(anyString(), anyString(), anyInt());

        // Capture token claims to assert expected contents
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String,Object>> claimsCap = ArgumentCaptor.forClass(Map.class);
        verify(tokenService).sign(claimsCap.capture(), any(Instant.class));
        Map<String,Object> claims = claimsCap.getValue();

        assertEquals("alice@example.com", claims.get("email")); // should be normalized lower-case
        assertEquals("NA", claims.get("codeHash"));             // special value when user not found
        assertFalse(claims.containsKey("userId"));
    }

    @Test
    @DisplayName("startLogin: user exists -> email is sent, token contains userId/email/codeHash(hex64)")
    void startLogin_userExists() {
        // Prepare an existing user
        UserEntity u = mock(UserEntity.class);
        when(u.getUserId()).thenReturn(101);
        when(u.getEmail()).thenReturn("bob@example.com");
        when(userRepo.findByEmailIgnoreCase("bob@example.com")).thenReturn(Optional.of(u));

        when(tokenService.sign(anyMap(), any(Instant.class))).thenReturn("JWT-BOB");

        LoginStartResponse resp = service.startLogin(new LoginStartRequest("bob@example.com"));

        // Response
        assertEquals("login code sent", resp.message());
        assertEquals("JWT-BOB", resp.challengeToken());

        // Email should be sent with TTL=10
        verify(emailService, times(1)).sendLoginCode(eq("bob@example.com"), anyString(), eq(10));

        // Claims should include userId/email/codeHash
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String,Object>> claimsCap = ArgumentCaptor.forClass(Map.class);
        verify(tokenService).sign(claimsCap.capture(), any(Instant.class));
        Map<String,Object> claims = claimsCap.getValue();

        assertEquals(101, claims.get("userId"));
        assertEquals("bob@example.com", claims.get("email"));

        // codeHash is a hex-encoded SHA-256 (64 hex chars). We can't know the exact value (random code),
        // but we can validate the shape safely.
        assertTrue(claims.containsKey("codeHash"));
        String codeHash = (String) claims.get("codeHash");
        assertNotNull(codeHash);
        assertTrue(Pattern.matches("^[0-9a-f]{64}$", codeHash));
    }

    // ===== verifyLogin() tests =====

    @Test
    @DisplayName("verifyLogin: happy path -> returns LoginSuccessResponse")
    void verifyLogin_ok() {
        // Given request
        String code = "123456";
        String email = "user@example.com";
        String jwt = "any-challenge";
        LoginVerifyRequest req = new LoginVerifyRequest(jwt, code, email, "Secr3tPwd!");

        // Prepare token claims returned by tokenService.verify
        Map<String,Object> claims = Map.of(
                "email", email,
                "codeHash", sha256Hex(code)  // MUST match the hash computed in service
        );
        when(tokenService.verify(jwt)).thenReturn(claims);

        // Prepare active user with matching password
        UserEntity user = mock(UserEntity.class);
        when(user.getIsActive()).thenReturn(true);
        when(user.getPasswordHash()).thenReturn("$hashed"); // any string
        when(user.getUserId()).thenReturn(2001);
        when(user.getNickname()).thenReturn("Sherry");
        when(user.getEmail()).thenReturn(email);

        when(userRepo.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secr3tPwd!", "$hashed")).thenReturn(true);

        LoginSuccessResponse resp = service.verifyLogin(req);

        assertNotNull(resp);
        assertEquals("successfully login!", resp.message());
        assertEquals(2001, resp.userId());
        assertEquals("Sherry", resp.nickname());
        assertEquals(email, resp.email());
    }

    @Test
    @DisplayName("verifyLogin: invalid/expired token -> throws InvalidCredentialsException")
    void verifyLogin_invalidToken_throws() {
        LoginVerifyRequest req = new LoginVerifyRequest("bad-token", "111111", "x@y.com", "pwd");
        // Simulate JWT verification failure
        io.jsonwebtoken.JwtException jwtEx = new io.jsonwebtoken.MalformedJwtException("bad");
        when(tokenService.verify("bad-token")).thenThrow(jwtEx);

        assertThrows(InvalidCredentialsException.class, () -> service.verifyLogin(req));
        verifyNoInteractions(userRepo, passwordEncoder); // should fail before touching repo/encoder
    }

    @Test
    @DisplayName("verifyLogin: email does not match token -> throws InvalidCredentialsException")
    void verifyLogin_emailMismatch_throws() {
        String jwt = "tok";
        String inputEmail = "a@b.com";
        Map<String,Object> claims = Map.of(
                "email", "other@b.com",
                "codeHash", sha256Hex("654321")
        );
        when(tokenService.verify(jwt)).thenReturn(claims);

        LoginVerifyRequest req = new LoginVerifyRequest(jwt, "654321", inputEmail, "pwd");
        assertThrows(InvalidCredentialsException.class, () -> service.verifyLogin(req));
        verifyNoInteractions(userRepo, passwordEncoder);
    }

    @Test
    @DisplayName("verifyLogin: password mismatch -> throws InvalidCredentialsException")
    void verifyLogin_badPassword_throws() {
        String code = "999999";
        String email = "me@site.com";
        String jwt = "tok";
        when(tokenService.verify(jwt)).thenReturn(Map.of(
                "email", email,
                "codeHash", sha256Hex(code)
        ));

        UserEntity user = mock(UserEntity.class);
        when(user.getIsActive()).thenReturn(true);
        when(user.getPasswordHash()).thenReturn("$hash");
        when(userRepo.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(eq("Pwd!"), eq("$hash"))).thenReturn(false);

        LoginVerifyRequest req = new LoginVerifyRequest(jwt, code, email, "Pwd!");
        assertThrows(InvalidCredentialsException.class, () -> service.verifyLogin(req));
    }

    // ===== helper: compute SHA-256 hex to mirror service behavior =====
    private static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
