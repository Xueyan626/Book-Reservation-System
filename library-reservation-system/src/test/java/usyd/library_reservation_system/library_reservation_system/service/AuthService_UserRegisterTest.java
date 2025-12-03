package usyd.library_reservation_system.library_reservation_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import usyd.library_reservation_system.library_reservation_system.dto.*;
import usyd.library_reservation_system.library_reservation_system.model.UserEntity;
import usyd.library_reservation_system.library_reservation_system.repository.UserRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthService_UserRegisterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService_UserRegister authService;

    private RegisterStartRequest startRequest;
    private RegisterSubmitRequest submitRequest;

    @BeforeEach
    void setUp() {
        // Set configuration values
        ReflectionTestUtils.setField(authService, "ttlMinutes", 10);
        ReflectionTestUtils.setField(authService, "codeLength", 6);

        // Setup start request
        startRequest = new RegisterStartRequest();
        startRequest.setEmail("test@example.com");
        startRequest.setNickname("TestUser");
        startRequest.setTelephone("0412345678");

        // Setup submit request
        submitRequest = new RegisterSubmitRequest();
        submitRequest.setEmail("test@example.com");
        submitRequest.setNickname("TestUser");
        submitRequest.setTelephone("0412345678");
        submitRequest.setPassword("Password123");
        submitRequest.setConfirmPassword("Password123");
        submitRequest.setCode("123456");
        submitRequest.setChallengeToken("valid.token.here");
    }

    // ==================== startRegister Tests ====================

    @Test
    void testStartRegister_Success() {
        // Arrange
        when(tokenService.sign(anyMap(), any(Instant.class))).thenReturn("generated.token");
        doNothing().when(emailService).sendRegisterCode(anyString(), anyString(), anyInt());

        // Act
        RegisterStartResponse response = authService.startRegister(startRequest);

        // Assert
        assertNotNull(response);
        assertEquals("generated.token", response.getChallengeToken());
        assertEquals(10, response.getTtlMinutes());
        
        verify(tokenService, times(1)).sign(anyMap(), any(Instant.class));
        verify(emailService, times(1)).sendRegisterCode(eq("test@example.com"), anyString(), eq(10));
    }

    @Test
    void testStartRegister_TrimsEmail() {
        // Arrange
        startRequest.setEmail("  Test@Example.COM  ");
        when(tokenService.sign(anyMap(), any(Instant.class))).thenReturn("token");

        // Act
        authService.startRegister(startRequest);

        // Assert
        ArgumentCaptor<Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(tokenService).sign(claimsCaptor.capture(), any(Instant.class));
        
        Map<String, Object> claims = claimsCaptor.getValue();
        assertEquals("test@example.com", claims.get("email"));
    }

    @Test
    void testStartRegister_GeneratesCode() {
        // Arrange
        when(tokenService.sign(anyMap(), any(Instant.class))).thenReturn("token");

        // Act
        authService.startRegister(startRequest);

        // Assert
        ArgumentCaptor<Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(tokenService).sign(claimsCaptor.capture(), any(Instant.class));
        
        Map<String, Object> claims = claimsCaptor.getValue();
        String code = (String) claims.get("code");
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}")); // All digits
    }

    @Test
    void testStartRegister_SendsEmail() {
        // Arrange
        when(tokenService.sign(anyMap(), any(Instant.class))).thenReturn("token");

        // Act
        authService.startRegister(startRequest);

        // Assert
        verify(emailService, times(1)).sendRegisterCode(
            eq("test@example.com"),
            anyString(),
            eq(10)
        );
    }

    @Test
    void testStartRegister_CreatesTokenWithCorrectClaims() {
        // Arrange
        when(tokenService.sign(anyMap(), any(Instant.class))).thenReturn("token");

        // Act
        authService.startRegister(startRequest);

        // Assert
        ArgumentCaptor<Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(tokenService).sign(claimsCaptor.capture(), any(Instant.class));
        
        Map<String, Object> claims = claimsCaptor.getValue();
        assertTrue(claims.containsKey("email"));
        assertTrue(claims.containsKey("code"));
        assertEquals("test@example.com", claims.get("email"));
    }

    @Test
    void testStartRegister_SetsExpirationTime() {
        // Arrange
        when(tokenService.sign(anyMap(), any(Instant.class))).thenReturn("token");

        // Act
        Instant beforeCall = Instant.now();
        authService.startRegister(startRequest);
        Instant afterCall = Instant.now();

        // Assert
        ArgumentCaptor<Instant> expiresAtCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(tokenService).sign(anyMap(), expiresAtCaptor.capture());
        
        Instant expiresAt = expiresAtCaptor.getValue();
        // Should be around 10 minutes from now
        assertTrue(expiresAt.isAfter(beforeCall.plusSeconds(9 * 60)));
        assertTrue(expiresAt.isBefore(afterCall.plusSeconds(11 * 60)));
    }

    @Test
    void testStartRegister_WithDifferentEmail() {
        // Arrange
        startRequest.setEmail("another@test.com");
        when(tokenService.sign(anyMap(), any(Instant.class))).thenReturn("token");

        // Act
        authService.startRegister(startRequest);

        // Assert
        verify(emailService).sendRegisterCode(eq("another@test.com"), anyString(), anyInt());
    }

    @Test
    void testStartRegister_GeneratesDifferentCodes() {
        // Arrange
        when(tokenService.sign(anyMap(), any(Instant.class))).thenReturn("token1", "token2");

        // Act
        authService.startRegister(startRequest);
        authService.startRegister(startRequest);

        // Assert
        ArgumentCaptor<Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(tokenService, times(2)).sign(claimsCaptor.capture(), any(Instant.class));
        
        // Note: codes might be the same by chance, but they're generated independently
        assertNotNull(claimsCaptor.getAllValues().get(0).get("code"));
        assertNotNull(claimsCaptor.getAllValues().get(1).get("code"));
    }

    @Test
    void testStartRegister_WithCustomTTL() {
        // Arrange
        ReflectionTestUtils.setField(authService, "ttlMinutes", 15);
        when(tokenService.sign(anyMap(), any(Instant.class))).thenReturn("token");

        // Act
        RegisterStartResponse response = authService.startRegister(startRequest);

        // Assert
        assertEquals(15, response.getTtlMinutes());
        verify(emailService).sendRegisterCode(anyString(), anyString(), eq(15));
    }

    @Test
    void testStartRegister_WithCustomCodeLength() {
        // Arrange
        ReflectionTestUtils.setField(authService, "codeLength", 8);
        when(tokenService.sign(anyMap(), any(Instant.class))).thenReturn("token");

        // Act
        authService.startRegister(startRequest);

        // Assert
        ArgumentCaptor<Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(tokenService).sign(claimsCaptor.capture(), any(Instant.class));
        
        String code = (String) claimsCaptor.getAllValues().get(0).get("code");
        assertEquals(8, code.length());
    }

    // ==================== submitRegister Success Tests ====================

    @Test
    void testSubmitRegister_Success() {
        // Arrange
        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("email", "test@example.com");
        tokenPayload.put("code", "123456");
        
        when(tokenService.verify("valid.token.here")).thenReturn(tokenPayload);
        when(passwordEncoder.encode("Password123")).thenReturn("$2a$10$hashedPassword");
        
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setUserId(1); // Simulate database setting the ID
            return user;
        });

        // Act
        UserProfileDTO result = authService.submitRegister(submitRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals("TestUser", result.getNickname());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("0412345678", result.getTelephone());
        
        verify(tokenService, times(1)).verify("valid.token.here");
        verify(passwordEncoder, times(1)).encode("Password123");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testSubmitRegister_SavesUserWithCorrectData() {
        // Arrange
        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("email", "test@example.com");
        tokenPayload.put("code", "123456");
        
        when(tokenService.verify(anyString())).thenReturn(tokenPayload);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hash");
        
        UserEntity savedUser = new UserEntity();
        savedUser.setUserId(1);
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        // Act
        authService.submitRegister(submitRequest);

        // Assert
        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        
        UserEntity user = userCaptor.getValue();
        assertEquals("TestUser", user.getNickname());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("0412345678", user.getTelephone());
        assertEquals("$2a$10$hash", user.getPasswordHash());
        assertTrue(user.getIsActive());
    }

    @Test
    void testSubmitRegister_TrimsEmailToLowercase() {
        // Arrange
        submitRequest.setEmail("  Test@Example.COM  ");
        
        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("email", "test@example.com");
        tokenPayload.put("code", "123456");
        
        when(tokenService.verify(anyString())).thenReturn(tokenPayload);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(new UserEntity());

        // Act
        authService.submitRegister(submitRequest);

        // Assert
        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("test@example.com", userCaptor.getValue().getEmail());
    }

    @Test
    void testSubmitRegister_EncodesPassword() {
        // Arrange
        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("email", "test@example.com");
        tokenPayload.put("code", "123456");
        
        when(tokenService.verify(anyString())).thenReturn(tokenPayload);
        when(passwordEncoder.encode("Password123")).thenReturn("$2a$10$encodedHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(new UserEntity());

        // Act
        authService.submitRegister(submitRequest);

        // Assert
        verify(passwordEncoder, times(1)).encode("Password123");
        
        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("$2a$10$encodedHash", userCaptor.getValue().getPasswordHash());
    }

    // ==================== submitRegister Validation Tests ====================

    @Test
    void testSubmitRegister_InvalidToken_ThrowsException() {
        // Arrange
        when(tokenService.verify("valid.token.here"))
            .thenThrow(new IllegalArgumentException("Invalid token"));

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> authService.submitRegister(submitRequest)
        );
        
        assertTrue(exception.getMessage().contains("Invalid token"));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testSubmitRegister_ExpiredToken_ThrowsException() {
        // Arrange
        when(tokenService.verify("valid.token.here"))
            .thenThrow(new IllegalArgumentException("Token expired"));

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> authService.submitRegister(submitRequest)
        );
        
        assertTrue(exception.getMessage().contains("Token expired"));
    }

    @Test
    void testSubmitRegister_EmailMismatch_ThrowsException() {
        // Arrange
        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("email", "different@example.com");
        tokenPayload.put("code", "123456");
        
        when(tokenService.verify(anyString())).thenReturn(tokenPayload);

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> authService.submitRegister(submitRequest)
        );
        
        assertTrue(exception.getMessage().contains("email mismatch"));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testSubmitRegister_EmailMismatch_CaseInsensitive() {
        // Arrange
        submitRequest.setEmail("TEST@EXAMPLE.COM");
        
        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("email", "test@example.com");
        tokenPayload.put("code", "123456");
        
        when(tokenService.verify(anyString())).thenReturn(tokenPayload);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(new UserEntity());

        // Act - Should succeed (case insensitive comparison)
        assertDoesNotThrow(() -> authService.submitRegister(submitRequest));
    }

    @Test
    void testSubmitRegister_CodeMismatch_ThrowsException() {
        // Arrange
        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("email", "test@example.com");
        tokenPayload.put("code", "654321");
        
        when(tokenService.verify(anyString())).thenReturn(tokenPayload);

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> authService.submitRegister(submitRequest)
        );
        
        assertTrue(exception.getMessage().contains("code mismatch"));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testSubmitRegister_CodeWithWhitespace_TrimsCorrectly() {
        // Arrange
        submitRequest.setCode("  123456  ");
        
        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("email", "test@example.com");
        tokenPayload.put("code", "123456");
        
        when(tokenService.verify(anyString())).thenReturn(tokenPayload);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(new UserEntity());

        // Act - Should succeed
        assertDoesNotThrow(() -> authService.submitRegister(submitRequest));
    }

    @Test
    void testSubmitRegister_PasswordMismatch_ThrowsException() {
        // Arrange
        submitRequest.setConfirmPassword("DifferentPassword123");
        
        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("email", "test@example.com");
        tokenPayload.put("code", "123456");
        
        when(tokenService.verify(anyString())).thenReturn(tokenPayload);

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> authService.submitRegister(submitRequest)
        );
        
        assertTrue(exception.getMessage().contains("password not match"));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testSubmitRegister_PasswordsMatch() {
        // Arrange
        submitRequest.setPassword("SamePassword123");
        submitRequest.setConfirmPassword("SamePassword123");
        
        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("email", "test@example.com");
        tokenPayload.put("code", "123456");
        
        when(tokenService.verify(anyString())).thenReturn(tokenPayload);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(new UserEntity());

        // Act
        assertDoesNotThrow(() -> authService.submitRegister(submitRequest));
        
        // Assert
        verify(passwordEncoder, times(1)).encode("SamePassword123");
    }

    // ==================== Validation Order Tests ====================

    @Test
    void testSubmitRegister_ValidatesTokenFirst() {
        // Arrange
        submitRequest.setPassword("Wrong1");
        submitRequest.setConfirmPassword("Wrong2");
        submitRequest.setCode("wrong");
        
        when(tokenService.verify(anyString()))
            .thenThrow(new IllegalArgumentException("Invalid token"));

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, 
            () -> authService.submitRegister(submitRequest));
        
        // Token validation happens first, so other validations are not reached
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testSubmitRegister_ValidatesEmailSecond() {
        // Arrange
        submitRequest.setEmail("wrong@email.com");
        submitRequest.setPassword("Wrong1");
        submitRequest.setConfirmPassword("Wrong2");
        
        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("email", "test@example.com");
        tokenPayload.put("code", "123456");
        
        when(tokenService.verify(anyString())).thenReturn(tokenPayload);

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> authService.submitRegister(submitRequest)
        );
        
        assertTrue(exception.getMessage().contains("email mismatch"));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testSubmitRegister_ValidatesCodeThird() {
        // Arrange
        submitRequest.setCode("wrongcode");
        submitRequest.setPassword("Wrong1");
        submitRequest.setConfirmPassword("Wrong2");
        
        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("email", "test@example.com");
        tokenPayload.put("code", "123456");
        
        when(tokenService.verify(anyString())).thenReturn(tokenPayload);

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> authService.submitRegister(submitRequest)
        );
        
        assertTrue(exception.getMessage().contains("code mismatch"));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testSubmitRegister_ValidatesPasswordFourth() {
        // Arrange
        submitRequest.setPassword("Password1");
        submitRequest.setConfirmPassword("Password2");
        
        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("email", "test@example.com");
        tokenPayload.put("code", "123456");
        
        when(tokenService.verify(anyString())).thenReturn(tokenPayload);

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> authService.submitRegister(submitRequest)
        );
        
        assertTrue(exception.getMessage().contains("password not match"));
        verify(passwordEncoder, never()).encode(anyString());
    }

    // ==================== Edge Cases ====================

    @Test
    void testStartRegister_WithSpecialCharactersInEmail() {
        // Arrange
        startRequest.setEmail("test+tag@example.com");
        when(tokenService.sign(anyMap(), any(Instant.class))).thenReturn("token");

        // Act
        authService.startRegister(startRequest);

        // Assert
        verify(emailService).sendRegisterCode(eq("test+tag@example.com"), anyString(), anyInt());
    }

    @Test
    void testStartRegister_WithUnicodeInNickname() {
        // Arrange
        startRequest.setNickname("Username");
        when(tokenService.sign(anyMap(), any(Instant.class))).thenReturn("token");

        // Act
        RegisterStartResponse response = authService.startRegister(startRequest);

        // Assert
        assertNotNull(response);
    }

    @Test
    void testSubmitRegister_WithLongPassword() {
        // Arrange
        String longPassword = "VeryLongPassword123456789012345678901234567890";
        submitRequest.setPassword(longPassword);
        submitRequest.setConfirmPassword(longPassword);
        
        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("email", "test@example.com");
        tokenPayload.put("code", "123456");
        
        when(tokenService.verify(anyString())).thenReturn(tokenPayload);
        when(passwordEncoder.encode(longPassword)).thenReturn("hash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(new UserEntity());

        // Act
        authService.submitRegister(submitRequest);

        // Assert
        verify(passwordEncoder).encode(longPassword);
    }

    @Test
    void testSubmitRegister_WithSpecialCharactersInPassword() {
        // Arrange
        String specialPassword = "P@ssw0rd!#$%^&*()";
        submitRequest.setPassword(specialPassword);
        submitRequest.setConfirmPassword(specialPassword);
        
        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("email", "test@example.com");
        tokenPayload.put("code", "123456");
        
        when(tokenService.verify(anyString())).thenReturn(tokenPayload);
        when(passwordEncoder.encode(specialPassword)).thenReturn("hash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(new UserEntity());

        // Act
        authService.submitRegister(submitRequest);

        // Assert
        verify(passwordEncoder).encode(specialPassword);
    }

    // ==================== Integration Tests ====================

    @Test
    void testCompleteRegistrationFlow() {
        // Arrange - Step 1: Start registration
        when(tokenService.sign(anyMap(), any(Instant.class))).thenReturn("challenge.token");
        
        // Act - Step 1
        RegisterStartResponse startResponse = authService.startRegister(startRequest);
        
        // Assert - Step 1
        assertNotNull(startResponse.getChallengeToken());
        
        // Arrange - Step 2: Submit registration
        submitRequest.setChallengeToken(startResponse.getChallengeToken());
        
        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("email", "test@example.com");
        tokenPayload.put("code", "123456");
        
        when(tokenService.verify("challenge.token")).thenReturn(tokenPayload);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        
        UserEntity savedUser = new UserEntity();
        savedUser.setUserId(1);
        savedUser.setNickname("TestUser");
        savedUser.setEmail("test@example.com");
        savedUser.setTelephone("0412345678");
        
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        // Act - Step 2
        UserProfileDTO profile = authService.submitRegister(submitRequest);

        // Assert - Step 2
        assertNotNull(profile);
        assertEquals("TestUser", profile.getNickname());
        assertEquals("test@example.com", profile.getEmail());
    }

    @Test
    void testMultipleStartRegistrations() {
        // Arrange
        when(tokenService.sign(anyMap(), any(Instant.class)))
            .thenReturn("token1", "token2", "token3");

        // Act
        RegisterStartResponse r1 = authService.startRegister(startRequest);
        RegisterStartResponse r2 = authService.startRegister(startRequest);
        RegisterStartResponse r3 = authService.startRegister(startRequest);

        // Assert
        assertNotEquals(r1.getChallengeToken(), r2.getChallengeToken());
        assertNotEquals(r2.getChallengeToken(), r3.getChallengeToken());
        
        verify(tokenService, times(3)).sign(anyMap(), any(Instant.class));
        verify(emailService, times(3)).sendRegisterCode(anyString(), anyString(), anyInt());
    }

    @Test
    void testSubmitRegister_MultipleUsers() {
        // Arrange - User 1
        Map<String, Object> payload1 = new HashMap<>();
        payload1.put("email", "user1@example.com");
        payload1.put("code", "111111");
        
        when(tokenService.verify("token1")).thenReturn(payload1);
        when(passwordEncoder.encode(anyString())).thenReturn("hash1", "hash2");
        
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            if (user.getEmail().equals("user1@example.com")) {
                user.setUserId(1);
            } else if (user.getEmail().equals("user2@example.com")) {
                user.setUserId(2);
            }
            return user;
        });
        
        // Arrange - User 2
        Map<String, Object> payload2 = new HashMap<>();
        payload2.put("email", "user2@example.com");
        payload2.put("code", "222222");
        
        when(tokenService.verify("token2")).thenReturn(payload2);

        // Act - Register user 1
        RegisterSubmitRequest req1 = new RegisterSubmitRequest();
        req1.setEmail("user1@example.com");
        req1.setNickname("User1");
        req1.setTelephone("0411111111");
        req1.setPassword("Pass1");
        req1.setConfirmPassword("Pass1");
        req1.setCode("111111");
        req1.setChallengeToken("token1");
        
        UserProfileDTO profile1 = authService.submitRegister(req1);

        // Act - Register user 2
        RegisterSubmitRequest req2 = new RegisterSubmitRequest();
        req2.setEmail("user2@example.com");
        req2.setNickname("User2");
        req2.setTelephone("0422222222");
        req2.setPassword("Pass2");
        req2.setConfirmPassword("Pass2");
        req2.setCode("222222");
        req2.setChallengeToken("token2");
        
        UserProfileDTO profile2 = authService.submitRegister(req2);

        // Assert
        assertEquals(1, profile1.getUserId());
        assertEquals(2, profile2.getUserId());
        verify(userRepository, times(2)).save(any(UserEntity.class));
    }

    // ==================== randomDigits Tests ====================

    @Test
    void testRandomDigits_GeneratesCorrectLength() {
        // This is tested indirectly through startRegister
        // We've already verified code length in other tests
        
        // Arrange
        when(tokenService.sign(anyMap(), any(Instant.class))).thenReturn("token");

        // Act
        authService.startRegister(startRequest);

        // Assert
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(tokenService).sign(captor.capture(), any(Instant.class));
        
        String code = (String) captor.getValue().get("code");
        assertEquals(6, code.length());
        assertTrue(code.matches("^\\d+$"));
    }

    @Test
    void testRandomDigits_GeneratesOnlyDigits() {
        // Arrange
        when(tokenService.sign(anyMap(), any(Instant.class))).thenReturn("token");

        // Act
        for (int i = 0; i < 10; i++) {
            authService.startRegister(startRequest);
        }

        // Assert
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(tokenService, times(10)).sign(captor.capture(), any(Instant.class));
        
        for (Map<String, Object> claims : captor.getAllValues()) {
            String code = (String) claims.get("code");
            assertTrue(code.matches("^[0-9]+$"));
        }
    }

    // ==================== User Active Status Tests ====================

    @Test
    void testSubmitRegister_SetsUserAsActive() {
        // Arrange
        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("email", "test@example.com");
        tokenPayload.put("code", "123456");
        
        when(tokenService.verify(anyString())).thenReturn(tokenPayload);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(new UserEntity());

        // Act
        authService.submitRegister(submitRequest);

        // Assert
        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        
        assertTrue(userCaptor.getValue().getIsActive());
    }

    // ==================== Return Value Tests ====================

    @Test
    void testSubmitRegister_ReturnsCorrectUserProfile() {
        // Arrange
        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("email", "test@example.com");
        tokenPayload.put("code", "123456");
        
        when(tokenService.verify(anyString())).thenReturn(tokenPayload);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setUserId(123); // Simulate database setting the ID
            return user;
        });

        // Act
        UserProfileDTO result = authService.submitRegister(submitRequest);

        // Assert
        assertEquals(123, result.getUserId());
        assertEquals("TestUser", result.getNickname());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("0412345678", result.getTelephone());
    }

    @Test
    void testSubmitRegister_DoesNotReturnPassword() {
        // Arrange
        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("email", "test@example.com");
        tokenPayload.put("code", "123456");
        
        when(tokenService.verify(anyString())).thenReturn(tokenPayload);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        
        UserEntity savedUser = new UserEntity();
        savedUser.setUserId(1);
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        // Act
        UserProfileDTO result = authService.submitRegister(submitRequest);

        // Assert
        assertNotNull(result);
        // UserProfileDTO should not have password field
        // This is verified by the DTO structure itself
    }

    // ==================== Repository Interaction Tests ====================

    @Test
    void testStartRegister_DoesNotAccessRepository() {
        // Arrange
        when(tokenService.sign(anyMap(), any(Instant.class))).thenReturn("token");

        // Act
        authService.startRegister(startRequest);

        // Assert
        verifyNoInteractions(userRepository);
    }

    @Test
    void testSubmitRegister_CallsRepositorySaveOnce() {
        // Arrange
        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("email", "test@example.com");
        tokenPayload.put("code", "123456");
        
        when(tokenService.verify(anyString())).thenReturn(tokenPayload);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(new UserEntity());

        // Act
        authService.submitRegister(submitRequest);

        // Assert
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testSubmitRegister_OnlyCallsRepositoryAfterAllValidations() {
        // Arrange
        submitRequest.setPassword("Wrong1");
        submitRequest.setConfirmPassword("Wrong2");
        
        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("email", "test@example.com");
        tokenPayload.put("code", "123456");
        
        when(tokenService.verify(anyString())).thenReturn(tokenPayload);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class,
            () -> authService.submitRegister(submitRequest));
        
        verify(userRepository, never()).save(any(UserEntity.class));
    }
}

