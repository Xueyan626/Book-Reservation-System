package usyd.library_reservation_system.library_reservation_system.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TokenServiceTest {

    private TokenService tokenService;
    private String testSecret;

    @BeforeEach
    void setup() {
        tokenService = new TokenService();
        // Use a sufficiently long secret key for HS256 (at least 256 bits = 32 bytes)
        testSecret = "9d3f2a6c4b8e1f0d7a5c3e1b9f2d4a6b1c3d5e7f9a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2";
        
        // Use ReflectionTestUtils to set private field
        ReflectionTestUtils.setField(tokenService, "secret", testSecret);
        
        // Manually call @PostConstruct method to initialize the key (using reflection)
        ReflectionTestUtils.invokeMethod(tokenService, "init");
    }

    @Test
    void testSign_Success() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 123);
        claims.put("email", "test@example.com");
        Instant expiresAt = Instant.now().plusSeconds(3600); // 1 hour from now

        // Act
        String token = tokenService.sign(claims, expiresAt);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        // JWT token 格式: header.payload.signature (3 parts separated by dots)
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    void testVerify_Success() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 456);
        claims.put("email", "user@example.com");
        claims.put("role", "user");
        Instant expiresAt = Instant.now().plusSeconds(3600); // 1 hour from now
        
        String token = tokenService.sign(claims, expiresAt);

        // Act
        Map<String, Object> verifiedClaims = tokenService.verify(token);

        // Assert
        assertNotNull(verifiedClaims);
        assertEquals(456, verifiedClaims.get("userId"));
        assertEquals("user@example.com", verifiedClaims.get("email"));
        assertEquals("user", verifiedClaims.get("role"));
        assertNotNull(verifiedClaims.get("exp")); // expiration time should be present
    }

    @Test
    void testSignAndVerify_WithMultipleClaims() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 789);
        claims.put("nickname", "TestUser");
        claims.put("isActive", true);
        claims.put("type", 0);
        Instant expiresAt = Instant.now().plusSeconds(7200); // 2 hours from now

        // Act
        String token = tokenService.sign(claims, expiresAt);
        Map<String, Object> verifiedClaims = tokenService.verify(token);

        // Assert
        assertEquals(789, verifiedClaims.get("userId"));
        assertEquals("TestUser", verifiedClaims.get("nickname"));
        assertEquals(true, verifiedClaims.get("isActive"));
        assertEquals(0, verifiedClaims.get("type"));
    }

    @Test
    void testVerify_ExpiredToken_ThrowsException() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 999);
        // Use expiration time in the past
        Instant expiresAt = Instant.now().minusSeconds(3600); // 1 hour ago (expired)
        
        String token = tokenService.sign(claims, expiresAt);

        // Act & Assert
        assertThrows(ExpiredJwtException.class, () -> {
            tokenService.verify(token);
        });
    }

    @Test
    void testVerify_InvalidToken_ThrowsException() {
        // Arrange - Use invalid token (wrong signature)
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEyMywiZW1haWwiOiJ0ZXN0QGV4YW1wbGUuY29tIn0.invalidSignature";

        // Act & Assert
        assertThrows(SignatureException.class, () -> {
            tokenService.verify(invalidToken);
        });
    }

    @Test
    void testVerify_MalformedToken_ThrowsException() {
        // Arrange - Use malformed token
        String malformedToken = "not.a.valid.jwt.token";

        // Act & Assert
        assertThrows(Exception.class, () -> { // May be IllegalArgumentException or other exception
            tokenService.verify(malformedToken);
        });
    }

    @Test
    void testSign_WithEmptyClaims() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        Instant expiresAt = Instant.now().plusSeconds(3600);

        // Act
        String token = tokenService.sign(claims, expiresAt);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // Verify token can be parsed
        Map<String, Object> verifiedClaims = tokenService.verify(token);
        assertNotNull(verifiedClaims);
        assertNotNull(verifiedClaims.get("exp"));
    }

    @Test
    void testSign_WithNullValues() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 100);
        claims.put("nullableField", null);
        Instant expiresAt = Instant.now().plusSeconds(3600);

        // Act
        String token = tokenService.sign(claims, expiresAt);
        Map<String, Object> verifiedClaims = tokenService.verify(token);

        // Assert
        assertNotNull(verifiedClaims);
        assertEquals(100, verifiedClaims.get("userId"));
        // null values may not be included in JWT claims
    }

    @Test
    void testSign_ExpiresAtInPast() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 200);
        // Although we pass an expiration time in the past, the sign method should still be able to generate a token
        Instant expiresAt = Instant.now().minusSeconds(60); // 1 minute ago

        // Act
        String token = tokenService.sign(claims, expiresAt);

        // Assert
        assertNotNull(token);
        
        // Verification should throw an expired exception
        assertThrows(ExpiredJwtException.class, () -> {
            tokenService.verify(token);
        });
    }

    @Test
    void testVerify_ValidTokenFromDifferentInstance() {
        // Arrange - Create another TokenService instance with the same secret
        TokenService anotherService = new TokenService();
        ReflectionTestUtils.setField(anotherService, "secret", testSecret);
        ReflectionTestUtils.invokeMethod(anotherService, "init");

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 300);
        Instant expiresAt = Instant.now().plusSeconds(3600);
        
        // Sign using another service
        String token = anotherService.sign(claims, expiresAt);

        // Act - Verify using current service
        Map<String, Object> verifiedClaims = tokenService.verify(token);

        // Assert - Should be able to verify because the same key is used
        assertNotNull(verifiedClaims);
        assertEquals(300, verifiedClaims.get("userId"));
    }

    @Test
    void testVerify_InvalidSignature_ThrowsException() {
        // Arrange - Create another TokenService instance with a different secret
        TokenService differentService = new TokenService();
        String differentSecret = "differentSecretKey123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
        ReflectionTestUtils.setField(differentService, "secret", differentSecret);
        ReflectionTestUtils.invokeMethod(differentService, "init");

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 400);
        Instant expiresAt = Instant.now().plusSeconds(3600);
        
        // Sign using service with different key
        String token = differentService.sign(claims, expiresAt);

        // Act & Assert - Verification with different key should fail
        assertThrows(SignatureException.class, () -> {
            tokenService.verify(token);
        });
    }
}
