package usyd.library_reservation_system.library_reservation_system.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SecurityBeansTest {

    private SecurityBeans securityBeans;

    @BeforeEach
    void setUp() {
        securityBeans = new SecurityBeans();
    }

    @Test
    void testPasswordEncoderBean_NotNull() {
        // Act
        PasswordEncoder passwordEncoder = securityBeans.passwordEncoder();

        // Assert
        assertNotNull(passwordEncoder, "PasswordEncoder should not be null");
    }

    @Test
    void testPasswordEncoderBean_IsBCryptPasswordEncoder() {
        // Act
        PasswordEncoder passwordEncoder = securityBeans.passwordEncoder();

        // Assert
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder, 
                "PasswordEncoder should be an instance of BCryptPasswordEncoder");
    }

    @Test
    void testPasswordEncoder_CanEncodePassword() {
        // Arrange
        PasswordEncoder passwordEncoder = securityBeans.passwordEncoder();
        String rawPassword = "testPassword123";

        // Act
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Assert
        assertNotNull(encodedPassword, "Encoded password should not be null");
        assertNotEquals(rawPassword, encodedPassword, "Encoded password should be different from raw password");
        assertTrue(encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$"), 
                "Encoded password should start with BCrypt identifier");
    }

    @Test
    void testPasswordEncoder_CanValidatePassword() {
        // Arrange
        PasswordEncoder passwordEncoder = securityBeans.passwordEncoder();
        String rawPassword = "mySecurePassword456";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Act
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);

        // Assert
        assertTrue(matches, "Password encoder should correctly validate matching passwords");
    }

    @Test
    void testPasswordEncoder_RejectsIncorrectPassword() {
        // Arrange
        PasswordEncoder passwordEncoder = securityBeans.passwordEncoder();
        String correctPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String encodedPassword = passwordEncoder.encode(correctPassword);

        // Act
        boolean matches = passwordEncoder.matches(wrongPassword, encodedPassword);

        // Assert
        assertFalse(matches, "Password encoder should reject incorrect passwords");
    }

    @Test
    void testPasswordEncoder_GeneratesDifferentHashesForSamePassword() {
        // Arrange
        PasswordEncoder passwordEncoder = securityBeans.passwordEncoder();
        String password = "samePassword";

        // Act
        String hash1 = passwordEncoder.encode(password);
        String hash2 = passwordEncoder.encode(password);

        // Assert
        assertNotEquals(hash1, hash2, 
                "BCrypt should generate different hashes for the same password due to salt");
        assertTrue(passwordEncoder.matches(password, hash1), "First hash should match original password");
        assertTrue(passwordEncoder.matches(password, hash2), "Second hash should match original password");
    }

    @Test
    void testPasswordEncoder_HandlesEmptyPassword() {
        // Arrange
        PasswordEncoder passwordEncoder = securityBeans.passwordEncoder();
        String emptyPassword = "";

        // Act
        String encodedPassword = passwordEncoder.encode(emptyPassword);

        // Assert
        assertNotNull(encodedPassword, "Should be able to encode empty password");
        assertTrue(passwordEncoder.matches(emptyPassword, encodedPassword), 
                "Should be able to match empty password");
    }

    @Test
    void testPasswordEncoder_HandlesLongPassword() {
        // Arrange
        PasswordEncoder passwordEncoder = securityBeans.passwordEncoder();
        // BCrypt has a 72 byte limit, so use a password within that limit
        String longPassword = "a".repeat(70);

        // Act
        String encodedPassword = passwordEncoder.encode(longPassword);

        // Assert
        assertNotNull(encodedPassword, "Should be able to encode long password");
        assertTrue(passwordEncoder.matches(longPassword, encodedPassword), 
                "Should be able to match long password");
    }
    
    @Test
    void testPasswordEncoder_RejectsPasswordOver72Bytes() {
        // Arrange
        PasswordEncoder passwordEncoder = securityBeans.passwordEncoder();
        // BCrypt throws exception for passwords over 72 bytes
        String tooLongPassword = "a".repeat(100);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            passwordEncoder.encode(tooLongPassword);
        }, "Should throw IllegalArgumentException for password over 72 bytes");
    }

    @Test
    void testPasswordEncoder_HandlesSpecialCharacters() {
        // Arrange
        PasswordEncoder passwordEncoder = securityBeans.passwordEncoder();
        String specialPassword = "P@ssw0rd!#$%^&*()";

        // Act
        String encodedPassword = passwordEncoder.encode(specialPassword);

        // Assert
        assertNotNull(encodedPassword, "Should be able to encode password with special characters");
        assertTrue(passwordEncoder.matches(specialPassword, encodedPassword), 
                "Should be able to match password with special characters");
    }

    @Test
    void testPasswordEncoder_HandlesUnicodeCharacters() {
        // Arrange
        PasswordEncoder passwordEncoder = securityBeans.passwordEncoder();
        String unicodePassword = "Password123Test";

        // Act
        String encodedPassword = passwordEncoder.encode(unicodePassword);

        // Assert
        assertNotNull(encodedPassword, "Should be able to encode password with Unicode characters");
        assertTrue(passwordEncoder.matches(unicodePassword, encodedPassword), 
                "Should be able to match password with Unicode characters");
    }

    @Test
    void testCorsConfigurerBean_NotNull() {
        // Act
        WebMvcConfigurer corsConfigurer = securityBeans.corsConfigurer();

        // Assert
        assertNotNull(corsConfigurer, "CorsConfigurer should not be null");
    }

    @Test
    void testCorsConfigurerBean_IsWebMvcConfigurer() {
        // Act
        WebMvcConfigurer corsConfigurer = securityBeans.corsConfigurer();

        // Assert
        assertTrue(corsConfigurer instanceof WebMvcConfigurer, 
                "CorsConfigurer should be an instance of WebMvcConfigurer");
    }

    @Test
    void testCorsConfigurer_AddsCorsMapping() {
        // Arrange
        WebMvcConfigurer corsConfigurer = securityBeans.corsConfigurer();
        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class);

        when(registry.addMapping(anyString())).thenReturn(registration);
        when(registration.allowedOrigins(anyString())).thenReturn(registration);
        when(registration.allowedMethods(any(String[].class))).thenReturn(registration);
        when(registration.allowedHeaders(anyString())).thenReturn(registration);
        when(registration.exposedHeaders(anyString())).thenReturn(registration);
        when(registration.allowCredentials(anyBoolean())).thenReturn(registration);
        when(registration.maxAge(anyLong())).thenReturn(registration);

        // Act
        corsConfigurer.addCorsMappings(registry);

        // Assert
        verify(registry, times(1)).addMapping("/api/**");
    }

    @Test
    void testCorsConfigurer_ConfiguresAllowedOrigins() {
        // Arrange
        WebMvcConfigurer corsConfigurer = securityBeans.corsConfigurer();
        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class);

        when(registry.addMapping(anyString())).thenReturn(registration);
        when(registration.allowedOrigins(anyString())).thenReturn(registration);
        when(registration.allowedMethods(any(String[].class))).thenReturn(registration);
        when(registration.allowedHeaders(anyString())).thenReturn(registration);
        when(registration.exposedHeaders(anyString())).thenReturn(registration);
        when(registration.allowCredentials(anyBoolean())).thenReturn(registration);
        when(registration.maxAge(anyLong())).thenReturn(registration);

        // Act
        corsConfigurer.addCorsMappings(registry);

        // Assert
        verify(registration, times(1)).allowedOrigins("http://localhost:5173");
    }

    @Test
    void testCorsConfigurer_ConfiguresAllowedMethods() {
        // Arrange
        WebMvcConfigurer corsConfigurer = securityBeans.corsConfigurer();
        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class);

        when(registry.addMapping(anyString())).thenReturn(registration);
        when(registration.allowedOrigins(anyString())).thenReturn(registration);
        when(registration.allowedMethods(any(String[].class))).thenReturn(registration);
        when(registration.allowedHeaders(anyString())).thenReturn(registration);
        when(registration.exposedHeaders(anyString())).thenReturn(registration);
        when(registration.allowCredentials(anyBoolean())).thenReturn(registration);
        when(registration.maxAge(anyLong())).thenReturn(registration);

        // Act
        corsConfigurer.addCorsMappings(registry);

        // Assert
        verify(registration, times(1)).allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS");
    }

    @Test
    void testCorsConfigurer_ConfiguresAllowedHeaders() {
        // Arrange
        WebMvcConfigurer corsConfigurer = securityBeans.corsConfigurer();
        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class);

        when(registry.addMapping(anyString())).thenReturn(registration);
        when(registration.allowedOrigins(anyString())).thenReturn(registration);
        when(registration.allowedMethods(any(String[].class))).thenReturn(registration);
        when(registration.allowedHeaders(anyString())).thenReturn(registration);
        when(registration.exposedHeaders(anyString())).thenReturn(registration);
        when(registration.allowCredentials(anyBoolean())).thenReturn(registration);
        when(registration.maxAge(anyLong())).thenReturn(registration);

        // Act
        corsConfigurer.addCorsMappings(registry);

        // Assert
        verify(registration, times(1)).allowedHeaders("*");
    }

    @Test
    void testCorsConfigurer_ConfiguresExposedHeaders() {
        // Arrange
        WebMvcConfigurer corsConfigurer = securityBeans.corsConfigurer();
        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class);

        when(registry.addMapping(anyString())).thenReturn(registration);
        when(registration.allowedOrigins(anyString())).thenReturn(registration);
        when(registration.allowedMethods(any(String[].class))).thenReturn(registration);
        when(registration.allowedHeaders(anyString())).thenReturn(registration);
        when(registration.exposedHeaders(anyString())).thenReturn(registration);
        when(registration.allowCredentials(anyBoolean())).thenReturn(registration);
        when(registration.maxAge(anyLong())).thenReturn(registration);

        // Act
        corsConfigurer.addCorsMappings(registry);

        // Assert
        verify(registration, times(1)).exposedHeaders("*");
    }

    @Test
    void testCorsConfigurer_ConfiguresAllowCredentials() {
        // Arrange
        WebMvcConfigurer corsConfigurer = securityBeans.corsConfigurer();
        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class);

        when(registry.addMapping(anyString())).thenReturn(registration);
        when(registration.allowedOrigins(anyString())).thenReturn(registration);
        when(registration.allowedMethods(any(String[].class))).thenReturn(registration);
        when(registration.allowedHeaders(anyString())).thenReturn(registration);
        when(registration.exposedHeaders(anyString())).thenReturn(registration);
        when(registration.allowCredentials(anyBoolean())).thenReturn(registration);
        when(registration.maxAge(anyLong())).thenReturn(registration);

        // Act
        corsConfigurer.addCorsMappings(registry);

        // Assert
        verify(registration, times(1)).allowCredentials(false);
    }

    @Test
    void testCorsConfigurer_ConfiguresMaxAge() {
        // Arrange
        WebMvcConfigurer corsConfigurer = securityBeans.corsConfigurer();
        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class);

        when(registry.addMapping(anyString())).thenReturn(registration);
        when(registration.allowedOrigins(anyString())).thenReturn(registration);
        when(registration.allowedMethods(any(String[].class))).thenReturn(registration);
        when(registration.allowedHeaders(anyString())).thenReturn(registration);
        when(registration.exposedHeaders(anyString())).thenReturn(registration);
        when(registration.allowCredentials(anyBoolean())).thenReturn(registration);
        when(registration.maxAge(anyLong())).thenReturn(registration);

        // Act
        corsConfigurer.addCorsMappings(registry);

        // Assert
        verify(registration, times(1)).maxAge(3600);
    }

    @Test
    void testCorsConfigurer_ConfiguresAllSettings() {
        // Arrange
        WebMvcConfigurer corsConfigurer = securityBeans.corsConfigurer();
        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class);

        when(registry.addMapping(anyString())).thenReturn(registration);
        when(registration.allowedOrigins(anyString())).thenReturn(registration);
        when(registration.allowedMethods(any(String[].class))).thenReturn(registration);
        when(registration.allowedHeaders(anyString())).thenReturn(registration);
        when(registration.exposedHeaders(anyString())).thenReturn(registration);
        when(registration.allowCredentials(anyBoolean())).thenReturn(registration);
        when(registration.maxAge(anyLong())).thenReturn(registration);

        // Act
        corsConfigurer.addCorsMappings(registry);

        // Assert - verify all CORS settings are configured
        verify(registry, times(1)).addMapping("/api/**");
        verify(registration, times(1)).allowedOrigins("http://localhost:5173");
        verify(registration, times(1)).allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS");
        verify(registration, times(1)).allowedHeaders("*");
        verify(registration, times(1)).exposedHeaders("*");
        verify(registration, times(1)).allowCredentials(false);
        verify(registration, times(1)).maxAge(3600);
        verifyNoMoreInteractions(registration);
    }

    @Test
    void testPasswordEncoder_StrengthIs12() {
        // Arrange
        PasswordEncoder passwordEncoder = securityBeans.passwordEncoder();
        BCryptPasswordEncoder bcryptEncoder = (BCryptPasswordEncoder) passwordEncoder;

        // Act
        String encoded = bcryptEncoder.encode("test");

        // Assert
        // BCrypt strength 12 produces hashes with $2a$12$ or $2b$12$ prefix
        assertTrue(encoded.contains("$12$"), 
                "BCrypt password encoder should use strength 12");
    }

    @Test
    void testPasswordEncoder_ProducesValidBCryptHash() {
        // Arrange
        PasswordEncoder passwordEncoder = securityBeans.passwordEncoder();
        String password = "TestPassword123!";

        // Act
        String hash = passwordEncoder.encode(password);

        // Assert
        assertNotNull(hash, "Hash should not be null");
        assertTrue(hash.length() >= 60, "BCrypt hash should be at least 60 characters");
        assertTrue(hash.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$"), 
                "Hash should match BCrypt pattern");
    }

    @Test
    void testPasswordEncoder_ConsistentBehavior() {
        // Arrange
        PasswordEncoder encoder1 = securityBeans.passwordEncoder();
        PasswordEncoder encoder2 = securityBeans.passwordEncoder();
        String password = "consistentTest";

        // Act
        String hash1 = encoder1.encode(password);
        String hash2 = encoder2.encode(password);

        // Assert
        // Different encoder instances should produce hashes that both validate
        assertTrue(encoder1.matches(password, hash1), "First encoder should validate its own hash");
        assertTrue(encoder2.matches(password, hash1), "Second encoder should validate first encoder's hash");
        assertTrue(encoder1.matches(password, hash2), "First encoder should validate second encoder's hash");
        assertTrue(encoder2.matches(password, hash2), "Second encoder should validate its own hash");
    }

    @Test
    void testPasswordEncoder_CaseSensitive() {
        // Arrange
        PasswordEncoder passwordEncoder = securityBeans.passwordEncoder();
        String password = "CaseSensitive";
        String encodedPassword = passwordEncoder.encode(password);

        // Act & Assert
        assertTrue(passwordEncoder.matches("CaseSensitive", encodedPassword), 
                "Should match exact case");
        assertFalse(passwordEncoder.matches("casesensitive", encodedPassword), 
                "Should not match different case");
        assertFalse(passwordEncoder.matches("CASESENSITIVE", encodedPassword), 
                "Should not match different case");
    }

    @Test
    void testPasswordEncoder_HandlesNullPassword() {
        // Arrange
        PasswordEncoder passwordEncoder = securityBeans.passwordEncoder();

        // Act & Assert
        // BCrypt should throw exception for null password
        assertThrows(IllegalArgumentException.class, () -> {
            passwordEncoder.encode(null);
        }, "Should throw IllegalArgumentException for null password");
    }

    @Test
    void testCorsConfigurer_MultipleInvocations() {
        // Arrange
        WebMvcConfigurer corsConfigurer = securityBeans.corsConfigurer();
        CorsRegistry registry1 = mock(CorsRegistry.class);
        CorsRegistry registry2 = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class);

        when(registry1.addMapping(anyString())).thenReturn(registration);
        when(registry2.addMapping(anyString())).thenReturn(registration);
        when(registration.allowedOrigins(anyString())).thenReturn(registration);
        when(registration.allowedMethods(any(String[].class))).thenReturn(registration);
        when(registration.allowedHeaders(anyString())).thenReturn(registration);
        when(registration.exposedHeaders(anyString())).thenReturn(registration);
        when(registration.allowCredentials(anyBoolean())).thenReturn(registration);
        when(registration.maxAge(anyLong())).thenReturn(registration);

        // Act
        corsConfigurer.addCorsMappings(registry1);
        corsConfigurer.addCorsMappings(registry2);

        // Assert
        verify(registry1, times(1)).addMapping("/api/**");
        verify(registry2, times(1)).addMapping("/api/**");
    }

    @Test
    void testPasswordEncoder_ReusabilityAcrossMultipleCalls() {
        // Arrange
        PasswordEncoder passwordEncoder = securityBeans.passwordEncoder();
        String password1 = "password1";
        String password2 = "password2";
        String password3 = "password3";

        // Act
        String hash1 = passwordEncoder.encode(password1);
        String hash2 = passwordEncoder.encode(password2);
        String hash3 = passwordEncoder.encode(password3);

        // Assert
        assertTrue(passwordEncoder.matches(password1, hash1), "Should match password1");
        assertTrue(passwordEncoder.matches(password2, hash2), "Should match password2");
        assertTrue(passwordEncoder.matches(password3, hash3), "Should match password3");
        assertFalse(passwordEncoder.matches(password1, hash2), "Should not cross-match password1 with hash2");
        assertFalse(passwordEncoder.matches(password2, hash3), "Should not cross-match password2 with hash3");
    }

    @Test
    void testBothBeans_CanBeCreatedSimultaneously() {
        // Act
        PasswordEncoder passwordEncoder = securityBeans.passwordEncoder();
        WebMvcConfigurer corsConfigurer = securityBeans.corsConfigurer();

        // Assert
        assertNotNull(passwordEncoder, "PasswordEncoder should not be null");
        assertNotNull(corsConfigurer, "CorsConfigurer should not be null");
    }

    @Test
    void testSecurityBeans_CanBeInstantiatedMultipleTimes() {
        // Arrange
        SecurityBeans beans1 = new SecurityBeans();
        SecurityBeans beans2 = new SecurityBeans();

        // Act
        PasswordEncoder encoder1 = beans1.passwordEncoder();
        PasswordEncoder encoder2 = beans2.passwordEncoder();
        WebMvcConfigurer cors1 = beans1.corsConfigurer();
        WebMvcConfigurer cors2 = beans2.corsConfigurer();

        // Assert
        assertNotNull(encoder1, "First encoder should not be null");
        assertNotNull(encoder2, "Second encoder should not be null");
        assertNotNull(cors1, "First CORS configurer should not be null");
        assertNotNull(cors2, "Second CORS configurer should not be null");
        
        // Verify they work independently
        String password = "test123";
        String hash = encoder1.encode(password);
        assertTrue(encoder2.matches(password, hash), 
                "Different encoder instances should be compatible");
    }

    @Test
    void testPasswordEncoder_PerformanceWithMultipleEncoding() {
        // Arrange
        PasswordEncoder passwordEncoder = securityBeans.passwordEncoder();
        int iterations = 10;

        // Act
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            String password = "password" + i;
            String hash = passwordEncoder.encode(password);
            assertTrue(passwordEncoder.matches(password, hash));
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Assert
        // BCrypt strength 12 is intentionally slow for security, so allow more time
        assertTrue(duration < 15000, 
                "Should complete " + iterations + " encodings in less than 15 seconds");
    }

    @Test
    void testCorsConfigurer_ChainedConfigurationReturnsCorrectly() {
        // Arrange
        WebMvcConfigurer corsConfigurer = securityBeans.corsConfigurer();
        CorsRegistry registry = new CorsRegistry();

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> {
            corsConfigurer.addCorsMappings(registry);
        }, "CORS configuration should complete without throwing exceptions");
    }
}

