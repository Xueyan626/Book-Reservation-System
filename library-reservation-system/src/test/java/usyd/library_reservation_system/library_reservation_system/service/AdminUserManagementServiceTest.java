package usyd.library_reservation_system.library_reservation_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import usyd.library_reservation_system.library_reservation_system.dto.AdminActionResponse;
import usyd.library_reservation_system.library_reservation_system.dto.AdminUserSummaryDTO;
import usyd.library_reservation_system.library_reservation_system.model.UserEntity;
import usyd.library_reservation_system.library_reservation_system.repository.UserRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserManagementService adminUserManagementService;

    private UserEntity testUser;
    private UserEntity testUser2;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setUserId(1);
        testUser.setNickname("alice");
        testUser.setEmail("alice@example.com");
        testUser.setTelephone("0412345678");
        testUser.setPasswordHash("$2a$12$hashedPassword");
        testUser.setIsActive(true);

        testUser2 = new UserEntity();
        testUser2.setUserId(2);
        testUser2.setNickname("bob");
        testUser2.setEmail("bob@example.com");
        testUser2.setTelephone("0423456789");
        testUser2.setPasswordHash("$2a$12$anotherHash");
        testUser2.setIsActive(false);
    }

    // ==================== searchByNickname Tests ====================

    @Test
    void testSearchByNickname_WithMatchingUsers() {
        // Arrange
        List<UserEntity> users = Arrays.asList(testUser, testUser2);
        when(userRepository.findByNicknameContainingIgnoreCase("ali")).thenReturn(users);

        // Act
        List<AdminUserSummaryDTO> result = adminUserManagementService.searchByNickname("ali");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("alice", result.get(0).getNickname());
        assertEquals("bob", result.get(1).getNickname());
        assertEquals("alice@example.com", result.get(0).getEmail());
        assertTrue(result.get(0).getIsActive());
        assertFalse(result.get(1).getIsActive());
        verify(userRepository, times(1)).findByNicknameContainingIgnoreCase("ali");
    }

    @Test
    void testSearchByNickname_WithNoMatches() {
        // Arrange
        when(userRepository.findByNicknameContainingIgnoreCase("nonexistent")).thenReturn(Collections.emptyList());

        // Act
        List<AdminUserSummaryDTO> result = adminUserManagementService.searchByNickname("nonexistent");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByNicknameContainingIgnoreCase("nonexistent");
    }

    @Test
    void testSearchByNickname_WithSingleMatch() {
        // Arrange
        when(userRepository.findByNicknameContainingIgnoreCase("alice")).thenReturn(Arrays.asList(testUser));

        // Act
        List<AdminUserSummaryDTO> result = adminUserManagementService.searchByNickname("alice");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getUserId());
        assertEquals("alice", result.get(0).getNickname());
        assertEquals("alice@example.com", result.get(0).getEmail());
        assertEquals("0412345678", result.get(0).getTelephone());
        assertTrue(result.get(0).getIsActive());
        verify(userRepository, times(1)).findByNicknameContainingIgnoreCase("alice");
    }

    @Test
    void testSearchByNickname_WithEmptyString() {
        // Arrange
        when(userRepository.findByNicknameContainingIgnoreCase("")).thenReturn(Arrays.asList(testUser, testUser2));

        // Act
        List<AdminUserSummaryDTO> result = adminUserManagementService.searchByNickname("");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findByNicknameContainingIgnoreCase("");
    }

    @Test
    void testSearchByNickname_CaseInsensitive() {
        // Arrange
        when(userRepository.findByNicknameContainingIgnoreCase("ALICE")).thenReturn(Arrays.asList(testUser));

        // Act
        List<AdminUserSummaryDTO> result = adminUserManagementService.searchByNickname("ALICE");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("alice", result.get(0).getNickname());
        verify(userRepository, times(1)).findByNicknameContainingIgnoreCase("ALICE");
    }

    @Test
    void testSearchByNickname_WithSpecialCharacters() {
        // Arrange
        UserEntity specialUser = new UserEntity();
        specialUser.setUserId(3);
        specialUser.setNickname("user@123");
        specialUser.setEmail("special@example.com");
        specialUser.setIsActive(true);

        when(userRepository.findByNicknameContainingIgnoreCase("@123")).thenReturn(Arrays.asList(specialUser));

        // Act
        List<AdminUserSummaryDTO> result = adminUserManagementService.searchByNickname("@123");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("user@123", result.get(0).getNickname());
        verify(userRepository, times(1)).findByNicknameContainingIgnoreCase("@123");
    }

    @Test
    void testSearchByNickname_DTOContainsAllFields() {
        // Arrange
        when(userRepository.findByNicknameContainingIgnoreCase("alice")).thenReturn(Arrays.asList(testUser));

        // Act
        List<AdminUserSummaryDTO> result = adminUserManagementService.searchByNickname("alice");

        // Assert
        AdminUserSummaryDTO dto = result.get(0);
        assertNotNull(dto.getUserId());
        assertNotNull(dto.getNickname());
        assertNotNull(dto.getEmail());
        assertNotNull(dto.getTelephone());
        assertNotNull(dto.getIsActive());
    }

    @Test
    void testSearchByNickname_PreservesActiveStatus() {
        // Arrange
        when(userRepository.findByNicknameContainingIgnoreCase("b")).thenReturn(Arrays.asList(testUser, testUser2));

        // Act
        List<AdminUserSummaryDTO> result = adminUserManagementService.searchByNickname("b");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.get(0).getIsActive(), "First user should be active");
        assertFalse(result.get(1).getIsActive(), "Second user should be inactive");
    }

    // ==================== resetPasswordToDefault Tests ====================

    @Test
    void testResetPasswordToDefault_Success() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("123456789")).thenReturn("$2a$12$newHashedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        AdminActionResponse result = adminUserManagementService.resetPasswordToDefault(1, "123456789");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertTrue(result.getMessage().contains("Password reset to default"));
        verify(userRepository, times(1)).findById(1);
        verify(passwordEncoder, times(1)).encode("123456789");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testResetPasswordToDefault_UserNotFound() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            adminUserManagementService.resetPasswordToDefault(999, "123456789");
        });

        assertTrue(exception.getMessage().contains("user not found"));
        assertTrue(exception.getMessage().contains("999"));
        verify(userRepository, times(1)).findById(999);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testResetPasswordToDefault_PasswordIsHashed() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("123456789")).thenReturn("$2a$12$hashedPassword123");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity savedUser = invocation.getArgument(0);
            assertEquals("$2a$12$hashedPassword123", savedUser.getPasswordHash());
            return savedUser;
        });

        // Act
        adminUserManagementService.resetPasswordToDefault(1, "123456789");

        // Assert
        verify(passwordEncoder, times(1)).encode("123456789");
        verify(userRepository, times(1)).save(argThat(user -> 
            "$2a$12$hashedPassword123".equals(user.getPasswordHash())
        ));
    }

    @Test
    void testResetPasswordToDefault_UpdatesUserInRepository() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$newHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        adminUserManagementService.resetPasswordToDefault(1, "newPassword");

        // Assert
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testResetPasswordToDefault_WithDifferentPassword() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("customPassword")).thenReturn("$2a$12$customHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        AdminActionResponse result = adminUserManagementService.resetPasswordToDefault(1, "customPassword");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getUserId());
        verify(passwordEncoder, times(1)).encode("customPassword");
    }

    @Test
    void testResetPasswordToDefault_ResponseContainsTimestamp() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        AdminActionResponse result = adminUserManagementService.resetPasswordToDefault(1, "123456789");

        // Assert
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().matches(".*\\d{4}-\\d{2}-\\d{2}.*"), 
                "Message should contain timestamp");
    }

    @Test
    void testResetPasswordToDefault_MultipleUsers() {
        // Arrange
        UserEntity user1 = new UserEntity();
        user1.setUserId(1);
        UserEntity user2 = new UserEntity();
        user2.setUserId(2);

        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2)).thenReturn(Optional.of(user2));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hash");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        AdminActionResponse result1 = adminUserManagementService.resetPasswordToDefault(1, "123456789");
        AdminActionResponse result2 = adminUserManagementService.resetPasswordToDefault(2, "123456789");

        // Assert
        assertEquals(1, result1.getUserId());
        assertEquals(2, result2.getUserId());
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(2);
        verify(userRepository, times(2)).save(any(UserEntity.class));
    }

    // ==================== banUser Tests ====================

    @Test
    void testBanUser_Success() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        adminUserManagementService.banUser(1);

        // Assert
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(argThat(user -> 
            Boolean.FALSE.equals(user.getIsActive())
        ));
    }

    @Test
    void testBanUser_UserNotFound() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            adminUserManagementService.banUser(999);
        });

        assertTrue(exception.getMessage().contains("user not found"));
        assertTrue(exception.getMessage().contains("999"));
        verify(userRepository, times(1)).findById(999);
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testBanUser_SetsIsActiveToFalse() {
        // Arrange
        testUser.setIsActive(true); // Initially active
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity savedUser = invocation.getArgument(0);
            assertFalse(savedUser.getIsActive(), "User should be set to inactive");
            return savedUser;
        });

        // Act
        adminUserManagementService.banUser(1);

        // Assert
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testBanUser_AlreadyBanned() {
        // Arrange
        testUser.setIsActive(false); // Already banned
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        adminUserManagementService.banUser(1);

        // Assert - Should still work, just sets isActive to false again
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testBanUser_WithDifferentUserId() {
        // Arrange
        when(userRepository.findById(5)).thenReturn(Optional.of(testUser2));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser2);

        // Act
        adminUserManagementService.banUser(5);

        // Assert
        verify(userRepository, times(1)).findById(5);
        verify(userRepository, times(1)).save(testUser2);
    }

    @Test
    void testBanUser_MultipleUsers() {
        // Arrange
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        adminUserManagementService.banUser(1);
        adminUserManagementService.banUser(2);
        adminUserManagementService.banUser(3);

        // Assert
        verify(userRepository, times(3)).findById(anyInt());
        verify(userRepository, times(3)).save(any(UserEntity.class));
    }

    @Test
    void testBanUser_DoesNotChangeOtherFields() {
        // Arrange
        String originalEmail = testUser.getEmail();
        String originalNickname = testUser.getNickname();
        String originalPassword = testUser.getPasswordHash();

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity savedUser = invocation.getArgument(0);
            assertEquals(originalEmail, savedUser.getEmail());
            assertEquals(originalNickname, savedUser.getNickname());
            assertEquals(originalPassword, savedUser.getPasswordHash());
            return savedUser;
        });

        // Act
        adminUserManagementService.banUser(1);

        // Assert
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    // ==================== unbanUser Tests ====================

    @Test
    void testUnbanUser_Success() {
        // Arrange
        testUser.setIsActive(false);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        adminUserManagementService.unbanUser(1);

        // Assert
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(argThat(user -> 
            Boolean.TRUE.equals(user.getIsActive())
        ));
    }

    @Test
    void testUnbanUser_UserNotFound() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            adminUserManagementService.unbanUser(999);
        });

        assertTrue(exception.getMessage().contains("user not found"));
        assertTrue(exception.getMessage().contains("999"));
        verify(userRepository, times(1)).findById(999);
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testUnbanUser_SetsIsActiveToTrue() {
        // Arrange
        testUser.setIsActive(false); // Initially banned
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity savedUser = invocation.getArgument(0);
            assertTrue(savedUser.getIsActive(), "User should be set to active");
            return savedUser;
        });

        // Act
        adminUserManagementService.unbanUser(1);

        // Assert
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testUnbanUser_AlreadyActive() {
        // Arrange
        testUser.setIsActive(true); // Already active
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        adminUserManagementService.unbanUser(1);

        // Assert - Should still work, just sets isActive to true again
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testUnbanUser_WithDifferentUserId() {
        // Arrange
        when(userRepository.findById(10)).thenReturn(Optional.of(testUser2));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser2);

        // Act
        adminUserManagementService.unbanUser(10);

        // Assert
        verify(userRepository, times(1)).findById(10);
        verify(userRepository, times(1)).save(testUser2);
    }

    @Test
    void testUnbanUser_MultipleUsers() {
        // Arrange
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        adminUserManagementService.unbanUser(1);
        adminUserManagementService.unbanUser(2);
        adminUserManagementService.unbanUser(3);

        // Assert
        verify(userRepository, times(3)).findById(anyInt());
        verify(userRepository, times(3)).save(any(UserEntity.class));
    }

    @Test
    void testUnbanUser_DoesNotChangeOtherFields() {
        // Arrange
        testUser.setIsActive(false);
        String originalEmail = testUser.getEmail();
        String originalNickname = testUser.getNickname();
        String originalPassword = testUser.getPasswordHash();

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity savedUser = invocation.getArgument(0);
            assertEquals(originalEmail, savedUser.getEmail());
            assertEquals(originalNickname, savedUser.getNickname());
            assertEquals(originalPassword, savedUser.getPasswordHash());
            return savedUser;
        });

        // Act
        adminUserManagementService.unbanUser(1);

        // Assert
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    // ==================== Ban and Unban Integration Tests ====================

    @Test
    void testBanThenUnban_Workflow() {
        // Arrange
        testUser.setIsActive(true);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        // Act - Ban
        adminUserManagementService.banUser(1);

        // Act - Unban
        adminUserManagementService.unbanUser(1);

        // Assert
        verify(userRepository, times(2)).findById(1);
        verify(userRepository, times(2)).save(testUser);
    }

    @Test
    void testUnbanThenBan_Workflow() {
        // Arrange
        testUser.setIsActive(false);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        // Act - Unban
        adminUserManagementService.unbanUser(1);

        // Act - Ban
        adminUserManagementService.banUser(1);

        // Assert
        verify(userRepository, times(2)).findById(1);
        verify(userRepository, times(2)).save(testUser);
    }

    // ==================== Search and Action Integration Tests ====================

    @Test
    void testSearchThenResetPassword_Workflow() {
        // Arrange
        when(userRepository.findByNicknameContainingIgnoreCase("alice")).thenReturn(Arrays.asList(testUser));
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        List<AdminUserSummaryDTO> searchResult = adminUserManagementService.searchByNickname("alice");
        AdminActionResponse resetResult = adminUserManagementService.resetPasswordToDefault(
                searchResult.get(0).getUserId(), "123456789");

        // Assert
        assertEquals(1, searchResult.size());
        assertEquals(1, resetResult.getUserId());
        verify(userRepository, times(1)).findByNicknameContainingIgnoreCase("alice");
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    void testSearchThenBan_Workflow() {
        // Arrange
        when(userRepository.findByNicknameContainingIgnoreCase("alice")).thenReturn(Arrays.asList(testUser));
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        List<AdminUserSummaryDTO> searchResult = adminUserManagementService.searchByNickname("alice");
        adminUserManagementService.banUser(searchResult.get(0).getUserId());

        // Assert
        assertEquals(1, searchResult.size());
        verify(userRepository, times(1)).findByNicknameContainingIgnoreCase("alice");
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    // ==================== Edge Cases and Validation Tests ====================

    @Test
    void testResetPassword_WithEmptyPassword() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("")).thenReturn("$2a$12$emptyHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        AdminActionResponse result = adminUserManagementService.resetPasswordToDefault(1, "");

        // Assert
        assertNotNull(result);
        verify(passwordEncoder, times(1)).encode("");
    }

    @Test
    void testResetPassword_WithLongPassword() {
        // Arrange
        String longPassword = "a".repeat(70);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(longPassword)).thenReturn("$2a$12$longHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        AdminActionResponse result = adminUserManagementService.resetPasswordToDefault(1, longPassword);

        // Assert
        assertNotNull(result);
        verify(passwordEncoder, times(1)).encode(longPassword);
    }

    @Test
    void testBanUser_WithInvalidUserId() {
        // Arrange
        when(userRepository.findById(-1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            adminUserManagementService.banUser(-1);
        });

        verify(userRepository, times(1)).findById(-1);
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testUnbanUser_WithInvalidUserId() {
        // Arrange
        when(userRepository.findById(-1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            adminUserManagementService.unbanUser(-1);
        });

        verify(userRepository, times(1)).findById(-1);
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    // ==================== Repository Interaction Tests ====================

    @Test
    void testAllMethods_CallRepository() {
        // Arrange
        when(userRepository.findByNicknameContainingIgnoreCase(anyString())).thenReturn(Arrays.asList(testUser));
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        adminUserManagementService.searchByNickname("test");
        adminUserManagementService.resetPasswordToDefault(1, "password");
        adminUserManagementService.banUser(1);
        adminUserManagementService.unbanUser(1);

        // Assert
        verify(userRepository, times(1)).findByNicknameContainingIgnoreCase(anyString());
        verify(userRepository, times(3)).findById(anyInt());
        verify(userRepository, times(3)).save(any(UserEntity.class));
    }

    @Test
    void testSearchByNickname_ReturnsImmutableList() {
        // Arrange
        when(userRepository.findByNicknameContainingIgnoreCase("alice")).thenReturn(Arrays.asList(testUser));

        // Act
        List<AdminUserSummaryDTO> result = adminUserManagementService.searchByNickname("alice");

        // Assert
        assertNotNull(result);
        // Verify it returns a list (toList() creates an immutable list in Java 16+)
        assertThrows(UnsupportedOperationException.class, () -> {
            result.add(AdminUserSummaryDTO.builder().build());
        });
    }

    @Test
    void testResetPassword_ResponseNotNull() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        AdminActionResponse result = adminUserManagementService.resetPasswordToDefault(1, "123456789");

        // Assert
        assertNotNull(result);
        assertNotNull(result.getUserId());
        assertNotNull(result.getMessage());
    }

    // ==================== Transactional Behavior Tests ====================

    @Test
    void testResetPassword_CallsRepositorySaveOnce() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        adminUserManagementService.resetPasswordToDefault(1, "123456789");

        // Assert - Should call save exactly once
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testBanUser_CallsRepositorySaveOnce() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        adminUserManagementService.banUser(1);

        // Assert - Should call save exactly once
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testUnbanUser_CallsRepositorySaveOnce() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        adminUserManagementService.unbanUser(1);

        // Assert - Should call save exactly once
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    // ==================== DTO Mapping Tests ====================

    @Test
    void testSearchByNickname_MapsAllDTOFields() {
        // Arrange
        when(userRepository.findByNicknameContainingIgnoreCase("alice")).thenReturn(Arrays.asList(testUser));

        // Act
        List<AdminUserSummaryDTO> result = adminUserManagementService.searchByNickname("alice");

        // Assert
        AdminUserSummaryDTO dto = result.get(0);
        assertEquals(testUser.getUserId(), dto.getUserId());
        assertEquals(testUser.getNickname(), dto.getNickname());
        assertEquals(testUser.getEmail(), dto.getEmail());
        assertEquals(testUser.getTelephone(), dto.getTelephone());
        assertEquals(testUser.getIsActive(), dto.getIsActive());
    }

    @Test
    void testSearchByNickname_HandlesNullTelephone() {
        // Arrange
        testUser.setTelephone(null);
        when(userRepository.findByNicknameContainingIgnoreCase("alice")).thenReturn(Arrays.asList(testUser));

        // Act
        List<AdminUserSummaryDTO> result = adminUserManagementService.searchByNickname("alice");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getTelephone());
    }

    @Test
    void testSearchByNickname_PreservesUserOrder() {
        // Arrange
        UserEntity user1 = new UserEntity();
        user1.setUserId(1);
        user1.setNickname("alice");
        user1.setEmail("alice@example.com");

        UserEntity user2 = new UserEntity();
        user2.setUserId(2);
        user2.setNickname("alicia");
        user2.setEmail("alicia@example.com");

        UserEntity user3 = new UserEntity();
        user3.setUserId(3);
        user3.setNickname("alison");
        user3.setEmail("alison@example.com");

        when(userRepository.findByNicknameContainingIgnoreCase("ali"))
                .thenReturn(Arrays.asList(user1, user2, user3));

        // Act
        List<AdminUserSummaryDTO> result = adminUserManagementService.searchByNickname("ali");

        // Assert
        assertEquals(3, result.size());
        assertEquals("alice", result.get(0).getNickname());
        assertEquals("alicia", result.get(1).getNickname());
        assertEquals("alison", result.get(2).getNickname());
    }
}

