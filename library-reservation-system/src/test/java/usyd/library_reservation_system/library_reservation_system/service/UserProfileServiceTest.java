package usyd.library_reservation_system.library_reservation_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import usyd.library_reservation_system.library_reservation_system.dto.ChangePasswordReq;
import usyd.library_reservation_system.library_reservation_system.dto.UserProfileDTO;
import usyd.library_reservation_system.library_reservation_system.model.UserEntity;
import usyd.library_reservation_system.library_reservation_system.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserProfileService userProfileService;

    private UserEntity testUser;
    private ChangePasswordReq validPasswordReq;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setUserId(1);
        testUser.setNickname("testUser");
        testUser.setEmail("test@example.com");
        testUser.setTelephone("0412345678");
        testUser.setPasswordHash("$2a$12$oldHashedPassword");
        testUser.setIsActive(true);

        validPasswordReq = new ChangePasswordReq();
        validPasswordReq.setCurrentPassword("OldPassword123");
        validPasswordReq.setNewPassword("NewPassword456");
        validPasswordReq.setConfirmNewPassword("NewPassword456");
    }

    // ==================== getProfile Tests ====================

    @Test
    void testGetProfile_Success() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // Act
        UserProfileDTO result = userProfileService.getProfile(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals("testUser", result.getNickname());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("0412345678", result.getTelephone());
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    void testGetProfile_UserNotFound() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.getProfile(999);
        });

        assertTrue(exception.getMessage().contains("User not found"));
        assertTrue(exception.getMessage().contains("999"));
        verify(userRepository, times(1)).findById(999);
    }

    @Test
    void testGetProfile_WithNullTelephone() {
        // Arrange
        testUser.setTelephone(null);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // Act
        UserProfileDTO result = userProfileService.getProfile(1);

        // Assert
        assertNotNull(result);
        assertNull(result.getTelephone());
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    void testGetProfile_AllFieldsMapped() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // Act
        UserProfileDTO result = userProfileService.getProfile(1);

        // Assert
        assertEquals(testUser.getUserId(), result.getUserId());
        assertEquals(testUser.getNickname(), result.getNickname());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getTelephone(), result.getTelephone());
    }

    @Test
    void testGetProfile_WithDifferentUserId() {
        // Arrange
        UserEntity anotherUser = new UserEntity();
        anotherUser.setUserId(5);
        anotherUser.setNickname("anotherUser");
        anotherUser.setEmail("another@example.com");
        when(userRepository.findById(5)).thenReturn(Optional.of(anotherUser));

        // Act
        UserProfileDTO result = userProfileService.getProfile(5);

        // Assert
        assertEquals(5, result.getUserId());
        assertEquals("anotherUser", result.getNickname());
        verify(userRepository, times(1)).findById(5);
    }

    @Test
    void testGetProfile_CallsRepositoryOnce() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // Act
        userProfileService.getProfile(1);

        // Assert
        verify(userRepository, times(1)).findById(1);
        verifyNoMoreInteractions(userRepository);
    }

    // ==================== changePassword Success Tests ====================

    @Test
    void testChangePassword_Success() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword456", "$2a$12$oldHashedPassword")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword456")).thenReturn("$2a$12$newHashedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        userProfileService.changePassword(1, validPasswordReq);

        // Assert
        verify(userRepository, times(1)).findById(1);
        verify(passwordEncoder, times(1)).matches("OldPassword123", "$2a$12$oldHashedPassword");
        verify(passwordEncoder, times(1)).encode("NewPassword456");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testChangePassword_UpdatesPasswordHash() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true, false);
        when(passwordEncoder.encode("NewPassword456")).thenReturn("$2a$12$newHash");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity savedUser = invocation.getArgument(0);
            assertEquals("$2a$12$newHash", savedUser.getPasswordHash());
            return savedUser;
        });

        // Act
        userProfileService.changePassword(1, validPasswordReq);

        // Assert
        verify(userRepository, times(1)).save(argThat(user -> 
            "$2a$12$newHash".equals(user.getPasswordHash())
        ));
    }

    // ==================== changePassword Validation Tests ====================

    @Test
    void testChangePassword_UserNotFound() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.changePassword(999, validPasswordReq);
        });

        assertTrue(exception.getMessage().contains("USER_NOT_FOUND"));
        assertTrue(exception.getMessage().contains("999"));
        verify(userRepository, times(1)).findById(999);
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testChangePassword_CurrentPasswordIncorrect() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.changePassword(1, validPasswordReq);
        });

        assertEquals("CURRENT_PASSWORD_INCORRECT", exception.getMessage());
        verify(passwordEncoder, times(1)).matches("OldPassword123", "$2a$12$oldHashedPassword");
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testChangePassword_NewPasswordMismatch() {
        // Arrange
        ChangePasswordReq mismatchReq = new ChangePasswordReq();
        mismatchReq.setCurrentPassword("OldPassword123");
        mismatchReq.setNewPassword("NewPassword456");
        mismatchReq.setConfirmNewPassword("DifferentPassword789");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.changePassword(1, mismatchReq);
        });

        assertEquals("NEW_PASSWORD_MISMATCH", exception.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testChangePassword_NewPasswordSameAsOld() {
        // Arrange
        ChangePasswordReq samePasswordReq = new ChangePasswordReq();
        samePasswordReq.setCurrentPassword("OldPassword123");
        samePasswordReq.setNewPassword("OldPassword123");
        samePasswordReq.setConfirmNewPassword("OldPassword123");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.changePassword(1, samePasswordReq);
        });

        assertEquals("NEW_PASSWORD_SAME_AS_OLD", exception.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testChangePassword_WeakPassword_TooShort() {
        // Arrange
        ChangePasswordReq weakReq = new ChangePasswordReq();
        weakReq.setCurrentPassword("OldPassword123");
        weakReq.setNewPassword("Short1"); // Only 6 chars, less than 8
        weakReq.setConfirmNewPassword("Short1");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.matches("Short1", "$2a$12$oldHashedPassword")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.changePassword(1, weakReq);
        });

        assertEquals("WEAK_PASSWORD", exception.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testChangePassword_WeakPassword_NoUpperCase() {
        // Arrange
        ChangePasswordReq weakReq = new ChangePasswordReq();
        weakReq.setCurrentPassword("OldPassword123");
        weakReq.setNewPassword("newpassword123"); // No uppercase
        weakReq.setConfirmNewPassword("newpassword123");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.matches("newpassword123", "$2a$12$oldHashedPassword")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.changePassword(1, weakReq);
        });

        assertEquals("WEAK_PASSWORD", exception.getMessage());
    }

    @Test
    void testChangePassword_WeakPassword_NoDigit() {
        // Arrange
        ChangePasswordReq weakReq = new ChangePasswordReq();
        weakReq.setCurrentPassword("OldPassword123");
        weakReq.setNewPassword("NewPassword"); // No digit
        weakReq.setConfirmNewPassword("NewPassword");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword", "$2a$12$oldHashedPassword")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.changePassword(1, weakReq);
        });

        assertEquals("WEAK_PASSWORD", exception.getMessage());
    }

    @Test
    void testChangePassword_WeakPassword_TooLong() {
        // Arrange
        ChangePasswordReq weakReq = new ChangePasswordReq();
        weakReq.setCurrentPassword("OldPassword123");
        String longPassword = "A1" + "a".repeat(70); // 72 chars, more than 64
        weakReq.setNewPassword(longPassword);
        weakReq.setConfirmNewPassword(longPassword);

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.matches(eq(longPassword), anyString())).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.changePassword(1, weakReq);
        });

        assertEquals("WEAK_PASSWORD", exception.getMessage());
    }

    @Test
    void testChangePassword_StrongPassword_MinimumRequirements() {
        // Arrange
        ChangePasswordReq minReq = new ChangePasswordReq();
        minReq.setCurrentPassword("OldPassword123");
        minReq.setNewPassword("Abcdefg1"); // Exactly 8 chars, has uppercase and digit
        minReq.setConfirmNewPassword("Abcdefg1");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.matches("Abcdefg1", "$2a$12$oldHashedPassword")).thenReturn(false);
        when(passwordEncoder.encode("Abcdefg1")).thenReturn("$2a$12$newHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        userProfileService.changePassword(1, minReq);

        // Assert
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testChangePassword_StrongPassword_MaximumLength() {
        // Arrange
        ChangePasswordReq maxReq = new ChangePasswordReq();
        maxReq.setCurrentPassword("OldPassword123");
        String maxPassword = "A1" + "a".repeat(62); // Exactly 64 chars
        maxReq.setNewPassword(maxPassword);
        maxReq.setConfirmNewPassword(maxPassword);

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.matches(eq(maxPassword), anyString())).thenReturn(false);
        when(passwordEncoder.encode(maxPassword)).thenReturn("$2a$12$newHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        userProfileService.changePassword(1, maxReq);

        // Assert
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testChangePassword_WithSpecialCharacters() {
        // Arrange
        ChangePasswordReq specialReq = new ChangePasswordReq();
        specialReq.setCurrentPassword("OldPassword123");
        specialReq.setNewPassword("NewPass@123!");
        specialReq.setConfirmNewPassword("NewPass@123!");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.matches("NewPass@123!", "$2a$12$oldHashedPassword")).thenReturn(false);
        when(passwordEncoder.encode("NewPass@123!")).thenReturn("$2a$12$newHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        userProfileService.changePassword(1, specialReq);

        // Assert
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    // ==================== Trim Handling Tests ====================

    @Test
    void testChangePassword_TrimsWhitespace() {
        // Arrange
        ChangePasswordReq reqWithSpaces = new ChangePasswordReq();
        reqWithSpaces.setCurrentPassword(" OldPassword123 ");
        reqWithSpaces.setNewPassword(" NewPassword456 ");
        reqWithSpaces.setConfirmNewPassword(" NewPassword456 ");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword456", "$2a$12$oldHashedPassword")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword456")).thenReturn("$2a$12$newHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        userProfileService.changePassword(1, reqWithSpaces);

        // Assert
        verify(passwordEncoder, times(1)).matches("OldPassword123", "$2a$12$oldHashedPassword");
        verify(passwordEncoder, times(1)).encode("NewPassword456");
    }

    @Test
    void testChangePassword_HandlesNullCurrentPassword() {
        // Arrange
        ChangePasswordReq nullReq = new ChangePasswordReq();
        nullReq.setCurrentPassword(null);
        nullReq.setNewPassword("NewPassword456");
        nullReq.setConfirmNewPassword("NewPassword456");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("", "$2a$12$oldHashedPassword")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.changePassword(1, nullReq);
        });

        assertEquals("CURRENT_PASSWORD_INCORRECT", exception.getMessage());
    }

    @Test
    void testChangePassword_HandlesNullNewPassword() {
        // Arrange
        ChangePasswordReq nullReq = new ChangePasswordReq();
        nullReq.setCurrentPassword("OldPassword123");
        nullReq.setNewPassword(null);
        nullReq.setConfirmNewPassword(null);

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.matches("", "$2a$12$oldHashedPassword")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.changePassword(1, nullReq);
        });

        assertEquals("WEAK_PASSWORD", exception.getMessage());
    }

    // ==================== Password Strength Validation Tests ====================

    @Test
    void testChangePassword_StrongPassword_WithMultipleUpperCase() {
        // Arrange
        ChangePasswordReq req = new ChangePasswordReq();
        req.setCurrentPassword("OldPassword123");
        req.setNewPassword("UPPERCASE123");
        req.setConfirmNewPassword("UPPERCASE123");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.matches("UPPERCASE123", "$2a$12$oldHashedPassword")).thenReturn(false);
        when(passwordEncoder.encode("UPPERCASE123")).thenReturn("$2a$12$newHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        userProfileService.changePassword(1, req);

        // Assert
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testChangePassword_StrongPassword_WithMultipleDigits() {
        // Arrange
        ChangePasswordReq req = new ChangePasswordReq();
        req.setCurrentPassword("OldPassword123");
        req.setNewPassword("Password123456");
        req.setConfirmNewPassword("Password123456");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.matches("Password123456", "$2a$12$oldHashedPassword")).thenReturn(false);
        when(passwordEncoder.encode("Password123456")).thenReturn("$2a$12$newHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        userProfileService.changePassword(1, req);

        // Assert
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testChangePassword_WeakPassword_OnlyLowercase() {
        // Arrange
        ChangePasswordReq weakReq = new ChangePasswordReq();
        weakReq.setCurrentPassword("OldPassword123");
        weakReq.setNewPassword("password123"); // No uppercase
        weakReq.setConfirmNewPassword("password123");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.matches("password123", "$2a$12$oldHashedPassword")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.changePassword(1, weakReq);
        });

        assertEquals("WEAK_PASSWORD", exception.getMessage());
    }

    @Test
    void testChangePassword_WeakPassword_OnlyUppercase() {
        // Arrange
        ChangePasswordReq weakReq = new ChangePasswordReq();
        weakReq.setCurrentPassword("OldPassword123");
        weakReq.setNewPassword("PASSWORD123"); // All uppercase but valid
        weakReq.setConfirmNewPassword("PASSWORD123");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.matches("PASSWORD123", "$2a$12$oldHashedPassword")).thenReturn(false);
        when(passwordEncoder.encode("PASSWORD123")).thenReturn("$2a$12$newHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act - Should succeed since it has uppercase and digit
        userProfileService.changePassword(1, weakReq);

        // Assert
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testChangePassword_WeakPassword_NoNumbers() {
        // Arrange
        ChangePasswordReq weakReq = new ChangePasswordReq();
        weakReq.setCurrentPassword("OldPassword123");
        weakReq.setNewPassword("NewPassword"); // No digits
        weakReq.setConfirmNewPassword("NewPassword");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword", "$2a$12$oldHashedPassword")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.changePassword(1, weakReq);
        });

        assertEquals("WEAK_PASSWORD", exception.getMessage());
    }

    // ==================== Validation Order Tests ====================

    @Test
    void testChangePassword_ValidatesInCorrectOrder_UserNotFoundFirst() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.changePassword(999, validPasswordReq);
        });

        assertTrue(exception.getMessage().contains("USER_NOT_FOUND"));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testChangePassword_ValidatesInCorrectOrder_CurrentPasswordSecond() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.changePassword(1, validPasswordReq);
        });

        assertEquals("CURRENT_PASSWORD_INCORRECT", exception.getMessage());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testChangePassword_ValidatesInCorrectOrder_MismatchThird() {
        // Arrange
        ChangePasswordReq mismatchReq = new ChangePasswordReq();
        mismatchReq.setCurrentPassword("OldPassword123");
        mismatchReq.setNewPassword("NewPassword456");
        mismatchReq.setConfirmNewPassword("Different789A");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.changePassword(1, mismatchReq);
        });

        assertEquals("NEW_PASSWORD_MISMATCH", exception.getMessage());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }

    // ==================== Multiple Users Tests ====================

    @Test
    void testGetProfile_MultipleUsers() {
        // Arrange
        UserEntity user1 = new UserEntity();
        user1.setUserId(1);
        user1.setNickname("user1");
        user1.setEmail("user1@example.com");

        UserEntity user2 = new UserEntity();
        user2.setUserId(2);
        user2.setNickname("user2");
        user2.setEmail("user2@example.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2)).thenReturn(Optional.of(user2));

        // Act
        UserProfileDTO result1 = userProfileService.getProfile(1);
        UserProfileDTO result2 = userProfileService.getProfile(2);

        // Assert
        assertEquals("user1", result1.getNickname());
        assertEquals("user2", result2.getNickname());
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(2);
    }

    @Test
    void testChangePassword_MultipleUsers() {
        // Arrange
        UserEntity user1 = new UserEntity();
        user1.setUserId(1);
        user1.setPasswordHash("$2a$12$hash1");

        UserEntity user2 = new UserEntity();
        user2.setUserId(2);
        user2.setPasswordHash("$2a$12$hash2");

        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2)).thenReturn(Optional.of(user2));
        
        // For user1: current password correct, new password different
        when(passwordEncoder.matches("OldPassword123", "$2a$12$hash1")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword456", "$2a$12$hash1")).thenReturn(false);
        
        // For user2: current password correct, new password different
        when(passwordEncoder.matches("OldPassword123", "$2a$12$hash2")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword456", "$2a$12$hash2")).thenReturn(false);
        
        when(passwordEncoder.encode("NewPassword456")).thenReturn("$2a$12$newHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(user1, user2);

        // Act
        userProfileService.changePassword(1, validPasswordReq);
        userProfileService.changePassword(2, validPasswordReq);

        // Assert
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(2);
        verify(userRepository, times(2)).save(any(UserEntity.class));
    }

    // ==================== Password Matching Tests ====================

    @Test
    void testChangePassword_ChecksOldPasswordBeforeChanging() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword456", "$2a$12$oldHashedPassword")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword456")).thenReturn("$2a$12$newHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        userProfileService.changePassword(1, validPasswordReq);

        // Assert - Should check old password first
        verify(passwordEncoder, times(1)).matches("OldPassword123", "$2a$12$oldHashedPassword");
        verify(passwordEncoder, times(1)).matches("NewPassword456", "$2a$12$oldHashedPassword");
        verify(passwordEncoder, times(1)).encode("NewPassword456");
    }

    @Test
    void testChangePassword_EncodesNewPassword() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true, false);
        when(passwordEncoder.encode("NewPassword456")).thenReturn("$2a$12$encodedNewPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        userProfileService.changePassword(1, validPasswordReq);

        // Assert
        verify(passwordEncoder, times(1)).encode("NewPassword456");
    }

    // ==================== Repository Save Tests ====================

    @Test
    void testChangePassword_SavesUpdatedUser() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true, false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$newHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        userProfileService.changePassword(1, validPasswordReq);

        // Assert
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testChangePassword_OnlyChangesPasswordHash() {
        // Arrange
        String originalNickname = testUser.getNickname();
        String originalEmail = testUser.getEmail();
        Integer originalUserId = testUser.getUserId();

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true, false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$newHash");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity savedUser = invocation.getArgument(0);
            assertEquals(originalUserId, savedUser.getUserId());
            assertEquals(originalNickname, savedUser.getNickname());
            assertEquals(originalEmail, savedUser.getEmail());
            return savedUser;
        });

        // Act
        userProfileService.changePassword(1, validPasswordReq);

        // Assert
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    // ==================== Edge Cases ====================

    @Test
    void testGetProfile_WithZeroUserId() {
        // Arrange
        when(userRepository.findById(0)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.getProfile(0);
        });

        verify(userRepository, times(1)).findById(0);
    }

    @Test
    void testGetProfile_WithNegativeUserId() {
        // Arrange
        when(userRepository.findById(-1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.getProfile(-1);
        });

        verify(userRepository, times(1)).findById(-1);
    }

    @Test
    void testChangePassword_WithZeroUserId() {
        // Arrange
        when(userRepository.findById(0)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.changePassword(0, validPasswordReq);
        });

        verify(userRepository, times(1)).findById(0);
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testChangePassword_WithNegativeUserId() {
        // Arrange
        when(userRepository.findById(-1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.changePassword(-1, validPasswordReq);
        });

        verify(userRepository, times(1)).findById(-1);
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    // ==================== Password Complexity Tests ====================

    @Test
    void testChangePassword_AcceptsPasswordWithMixedCase() {
        // Arrange
        ChangePasswordReq req = new ChangePasswordReq();
        req.setCurrentPassword("OldPassword123");
        req.setNewPassword("MiXeDCaSe123");
        req.setConfirmNewPassword("MiXeDCaSe123");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.matches("MiXeDCaSe123", "$2a$12$oldHashedPassword")).thenReturn(false);
        when(passwordEncoder.encode("MiXeDCaSe123")).thenReturn("$2a$12$newHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        userProfileService.changePassword(1, req);

        // Assert
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testChangePassword_AcceptsPasswordWithSymbols() {
        // Arrange
        ChangePasswordReq req = new ChangePasswordReq();
        req.setCurrentPassword("OldPassword123");
        req.setNewPassword("Passw0rd!@#$%");
        req.setConfirmNewPassword("Passw0rd!@#$%");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.matches("Passw0rd!@#$%", "$2a$12$oldHashedPassword")).thenReturn(false);
        when(passwordEncoder.encode("Passw0rd!@#$%")).thenReturn("$2a$12$newHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        userProfileService.changePassword(1, req);

        // Assert
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    // ==================== Integration Tests ====================

    @Test
    void testGetProfileThenChangePassword_Workflow() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true, false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$newHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        UserProfileDTO profile = userProfileService.getProfile(1);
        userProfileService.changePassword(profile.getUserId(), validPasswordReq);

        // Assert
        assertEquals(1, profile.getUserId());
        verify(userRepository, times(2)).findById(1);
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testChangePassword_DoesNotCallGetProfile() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true, false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$newHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        userProfileService.changePassword(1, validPasswordReq);

        // Assert - Only findById should be called, not getProfile
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    // ==================== Transactional Behavior Tests ====================

    @Test
    void testChangePassword_IsTransactional() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true, false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$newHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        userProfileService.changePassword(1, validPasswordReq);

        // Assert - Should save exactly once in a transaction
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testChangePassword_DoesNotSaveOnValidationFailure() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.changePassword(1, validPasswordReq);
        });

        // Should not save when validation fails
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    // ==================== Comprehensive Tests ====================

    @Test
    void testAllMethods_CallRepository() {
        // Arrange
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true, false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$newHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        userProfileService.getProfile(1);
        userProfileService.changePassword(1, validPasswordReq);

        // Assert
        verify(userRepository, times(2)).findById(anyInt());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testGetProfile_ReturnsNonNullDTO() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // Act
        UserProfileDTO result = userProfileService.getProfile(1);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getUserId());
        assertNotNull(result.getNickname());
        assertNotNull(result.getEmail());
    }

    @Test
    void testChangePassword_SuccessfullyUpdatesHash() {
        // Arrange
        String newHash = "$2a$12$brandNewHash";
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword456", "$2a$12$oldHashedPassword")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword456")).thenReturn(newHash);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity saved = invocation.getArgument(0);
            assertEquals(newHash, saved.getPasswordHash());
            return saved;
        });

        // Act
        userProfileService.changePassword(1, validPasswordReq);

        // Assert
        verify(userRepository, times(1)).save(argThat(user -> 
            newHash.equals(user.getPasswordHash())
        ));
    }
}

