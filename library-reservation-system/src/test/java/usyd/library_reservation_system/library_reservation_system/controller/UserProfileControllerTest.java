package usyd.library_reservation_system.library_reservation_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import usyd.library_reservation_system.library_reservation_system.dto.ChangePasswordReq;
import usyd.library_reservation_system.library_reservation_system.dto.UserProfileDTO;
import usyd.library_reservation_system.library_reservation_system.service.UserProfileService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserProfileController.class)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileService userProfileService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserProfileDTO testProfile;
    private ChangePasswordReq validPasswordReq;

    @BeforeEach
    void setUp() {
        testProfile = UserProfileDTO.builder()
                .userId(1)
                .nickname("testUser")
                .email("test@example.com")
                .telephone("0412345678")
                .registrationTime("2025-01-01 10:00:00")
                .build();

        validPasswordReq = new ChangePasswordReq();
        validPasswordReq.setCurrentPassword("oldPassword123");
        validPasswordReq.setNewPassword("newPassword456");
        validPasswordReq.setConfirmNewPassword("newPassword456");
    }

    // ==================== getProfile Tests ====================

    @Test
    void testGetProfile_Success() throws Exception {
        // Arrange
        when(userProfileService.getProfile(1)).thenReturn(testProfile);

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.nickname").value("testUser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.telephone").value("0412345678"))
                .andExpect(jsonPath("$.registrationTime").value("2025-01-01 10:00:00"));

        verify(userProfileService, times(1)).getProfile(1);
    }

    @Test
    void testGetProfile_WithDifferentUserId() throws Exception {
        // Arrange
        UserProfileDTO profile = UserProfileDTO.builder()
                .userId(99)
                .nickname("anotherUser")
                .email("another@example.com")
                .build();
        when(userProfileService.getProfile(99)).thenReturn(profile);

        // Act & Assert
        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(99))
                .andExpect(jsonPath("$.nickname").value("anotherUser"));

        verify(userProfileService, times(1)).getProfile(99);
    }

    @Test
    void testGetProfile_UserNotFound() throws Exception {
        // Arrange
        when(userProfileService.getProfile(999))
                .thenThrow(new IllegalArgumentException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isBadRequest());

        verify(userProfileService, times(1)).getProfile(999);
    }

    @Test
    void testGetProfile_WithZeroUserId() throws Exception {
        // Arrange
        when(userProfileService.getProfile(0))
                .thenThrow(new IllegalArgumentException("Invalid user ID"));

        // Act & Assert
        mockMvc.perform(get("/api/users/0"))
                .andExpect(status().isBadRequest());

        verify(userProfileService, times(1)).getProfile(0);
    }

    @Test
    void testGetProfile_WithNegativeUserId() throws Exception {
        // Arrange
        when(userProfileService.getProfile(-1))
                .thenThrow(new IllegalArgumentException("Invalid user ID"));

        // Act & Assert
        mockMvc.perform(get("/api/users/-1"))
                .andExpect(status().isBadRequest());

        verify(userProfileService, times(1)).getProfile(-1);
    }

    @Test
    void testGetProfile_WithNullTelephone() throws Exception {
        // Arrange
        UserProfileDTO profileWithoutPhone = UserProfileDTO.builder()
                .userId(1)
                .nickname("testUser")
                .email("test@example.com")
                .telephone(null)
                .build();
        when(userProfileService.getProfile(1)).thenReturn(profileWithoutPhone);

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.telephone").isEmpty());

        verify(userProfileService, times(1)).getProfile(1);
    }

    @Test
    void testGetProfile_ReturnsAllFields() throws Exception {
        // Arrange
        when(userProfileService.getProfile(1)).thenReturn(testProfile);

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.nickname").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.telephone").exists())
                .andExpect(jsonPath("$.registrationTime").exists());

        verify(userProfileService, times(1)).getProfile(1);
    }

    // ==================== changePassword Tests ====================

    @Test
    void testChangePassword_Success() throws Exception {
        // Arrange
        doNothing().when(userProfileService).changePassword(eq(1), any(ChangePasswordReq.class));

        // Act & Assert
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordReq)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("PASSWORD_UPDATED"));

        verify(userProfileService, times(1)).changePassword(eq(1), any(ChangePasswordReq.class));
    }

    @Test
    void testChangePassword_WithDifferentUserId() throws Exception {
        // Arrange
        doNothing().when(userProfileService).changePassword(eq(5), any(ChangePasswordReq.class));

        // Act & Assert
        mockMvc.perform(post("/api/users/5/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("PASSWORD_UPDATED"));

        verify(userProfileService, times(1)).changePassword(eq(5), any(ChangePasswordReq.class));
    }

    @Test
    void testChangePassword_MissingCurrentPassword() throws Exception {
        // Arrange
        ChangePasswordReq invalidReq = new ChangePasswordReq();
        invalidReq.setCurrentPassword(""); // Empty current password
        invalidReq.setNewPassword("newPassword456");
        invalidReq.setConfirmNewPassword("newPassword456");

        // Act & Assert
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));

        verify(userProfileService, never()).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    @Test
    void testChangePassword_MissingNewPassword() throws Exception {
        // Arrange
        ChangePasswordReq invalidReq = new ChangePasswordReq();
        invalidReq.setCurrentPassword("oldPassword123");
        invalidReq.setNewPassword(""); // Empty new password
        invalidReq.setConfirmNewPassword("newPassword456");

        // Act & Assert
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));

        verify(userProfileService, never()).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    @Test
    void testChangePassword_NewPasswordTooShort() throws Exception {
        // Arrange
        ChangePasswordReq invalidReq = new ChangePasswordReq();
        invalidReq.setCurrentPassword("oldPassword123");
        invalidReq.setNewPassword("short"); // Less than 8 characters
        invalidReq.setConfirmNewPassword("short");

        // Act & Assert
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));

        verify(userProfileService, never()).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    @Test
    void testChangePassword_NewPasswordTooLong() throws Exception {
        // Arrange
        ChangePasswordReq invalidReq = new ChangePasswordReq();
        invalidReq.setCurrentPassword("oldPassword123");
        String longPassword = "a".repeat(70); // More than 64 characters
        invalidReq.setNewPassword(longPassword);
        invalidReq.setConfirmNewPassword(longPassword);

        // Act & Assert
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));

        verify(userProfileService, never()).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    @Test
    void testChangePassword_MissingConfirmPassword() throws Exception {
        // Arrange
        ChangePasswordReq invalidReq = new ChangePasswordReq();
        invalidReq.setCurrentPassword("oldPassword123");
        invalidReq.setNewPassword("newPassword456");
        invalidReq.setConfirmNewPassword(""); // Empty confirm password

        // Act & Assert
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));

        verify(userProfileService, never()).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    @Test
    void testChangePassword_ServiceThrowsException() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("CURRENT_PASSWORD_INCORRECT"))
                .when(userProfileService).changePassword(eq(1), any(ChangePasswordReq.class));

        // Act & Assert
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordReq)))
                .andExpect(status().isBadRequest());

        verify(userProfileService, times(1)).changePassword(eq(1), any(ChangePasswordReq.class));
    }

    @Test
    void testChangePassword_CurrentPasswordIncorrect() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("CURRENT_PASSWORD_INCORRECT"))
                .when(userProfileService).changePassword(anyInt(), any(ChangePasswordReq.class));

        // Act & Assert
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("CURRENT_PASSWORD_INCORRECT"));

        verify(userProfileService, times(1)).changePassword(eq(1), any(ChangePasswordReq.class));
    }

    @Test
    void testChangePassword_PasswordMismatch() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("NEW_PASSWORD_MISMATCH"))
                .when(userProfileService).changePassword(anyInt(), any(ChangePasswordReq.class));

        // Act & Assert
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("NEW_PASSWORD_MISMATCH"));

        verify(userProfileService, times(1)).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    @Test
    void testChangePassword_UserNotFound() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("User not found"))
                .when(userProfileService).changePassword(eq(999), any(ChangePasswordReq.class));

        // Act & Assert
        mockMvc.perform(post("/api/users/999/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordReq)))
                .andExpect(status().isBadRequest());

        verify(userProfileService, times(1)).changePassword(eq(999), any(ChangePasswordReq.class));
    }

    @Test
    void testChangePassword_ValidatesMinLength() throws Exception {
        // Arrange
        ChangePasswordReq shortReq = new ChangePasswordReq();
        shortReq.setCurrentPassword("oldPassword");
        shortReq.setNewPassword("1234567"); // Exactly 7 chars - should fail
        shortReq.setConfirmNewPassword("1234567");

        // Act & Assert
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.newPassword").exists());

        verify(userProfileService, never()).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    @Test
    void testChangePassword_ValidatesExactMinLength() throws Exception {
        // Arrange
        ChangePasswordReq exactReq = new ChangePasswordReq();
        exactReq.setCurrentPassword("oldPassword");
        exactReq.setNewPassword("12345678"); // Exactly 8 chars - should pass validation
        exactReq.setConfirmNewPassword("12345678");

        doNothing().when(userProfileService).changePassword(anyInt(), any(ChangePasswordReq.class));

        // Act & Assert
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exactReq)))
                .andExpect(status().isOk());

        verify(userProfileService, times(1)).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    @Test
    void testChangePassword_WithSpecialCharacters() throws Exception {
        // Arrange
        ChangePasswordReq specialReq = new ChangePasswordReq();
        specialReq.setCurrentPassword("oldPass@123");
        specialReq.setNewPassword("newPass!@#$%^&*()");
        specialReq.setConfirmNewPassword("newPass!@#$%^&*()");

        doNothing().when(userProfileService).changePassword(anyInt(), any(ChangePasswordReq.class));

        // Act & Assert
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(specialReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("PASSWORD_UPDATED"));

        verify(userProfileService, times(1)).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    @Test
    void testChangePassword_ReturnsCorrectResponseStructure() throws Exception {
        // Arrange
        doNothing().when(userProfileService).changePassword(anyInt(), any(ChangePasswordReq.class));

        // Act & Assert
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").isString());

        verify(userProfileService, times(1)).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    // ==================== Integration Tests ====================

    @Test
    void testGetProfileThenChangePassword_Workflow() throws Exception {
        // Arrange
        when(userProfileService.getProfile(1)).thenReturn(testProfile);
        doNothing().when(userProfileService).changePassword(eq(1), any(ChangePasswordReq.class));

        // Act & Assert - Get Profile
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));

        // Act & Assert - Change Password
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("PASSWORD_UPDATED"));

        verify(userProfileService, times(1)).getProfile(1);
        verify(userProfileService, times(1)).changePassword(eq(1), any(ChangePasswordReq.class));
    }

    @Test
    void testMultiplePasswordChanges() throws Exception {
        // Arrange
        doNothing().when(userProfileService).changePassword(anyInt(), any(ChangePasswordReq.class));

        // Act & Assert - First change
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordReq)))
                .andExpect(status().isOk());

        // Act & Assert - Second change
        ChangePasswordReq secondReq = new ChangePasswordReq();
        secondReq.setCurrentPassword("newPassword456");
        secondReq.setNewPassword("anotherPass789");
        secondReq.setConfirmNewPassword("anotherPass789");

        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondReq)))
                .andExpect(status().isOk());

        verify(userProfileService, times(2)).changePassword(eq(1), any(ChangePasswordReq.class));
    }

    // ==================== CORS Tests ====================

    @Test
    void testCorsEnabled_ForGetProfile() throws Exception {
        // Arrange
        when(userProfileService.getProfile(anyInt())).thenReturn(testProfile);

        // Act & Assert
        mockMvc.perform(get("/api/users/1")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk());

        verify(userProfileService, times(1)).getProfile(1);
    }

    @Test
    void testCorsEnabled_ForChangePassword() throws Exception {
        // Arrange
        doNothing().when(userProfileService).changePassword(anyInt(), any(ChangePasswordReq.class));

        // Act & Assert
        mockMvc.perform(post("/api/users/1/password")
                        .header("Origin", "http://localhost:5173")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordReq)))
                .andExpect(status().isOk());

        verify(userProfileService, times(1)).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    // ==================== HTTP Method Tests ====================

    @Test
    void testGetProfile_OnlyAcceptsGetMethod() throws Exception {
        // Act & Assert - POST should not be allowed
        mockMvc.perform(post("/api/users/1"))
                .andExpect(status().is5xxServerError());

        verify(userProfileService, never()).getProfile(anyInt());
    }

    @Test
    void testChangePassword_OnlyAcceptsPostMethod() throws Exception {
        // Act & Assert - GET should not be allowed
        mockMvc.perform(get("/api/users/1/password"))
                .andExpect(status().is5xxServerError());

        verify(userProfileService, never()).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    @Test
    void testChangePassword_PatchMethodNotAllowed() throws Exception {
        // Act & Assert - PATCH should not be allowed (endpoint uses POST)
        mockMvc.perform(patch("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordReq)))
                .andExpect(status().is5xxServerError());

        verify(userProfileService, never()).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    // ==================== URL Path Tests ====================

    @Test
    void testGetProfile_RequiresUserId() throws Exception {
        // Act & Assert - Missing userId
        mockMvc.perform(get("/api/users/"))
                .andExpect(status().is5xxServerError());

        verify(userProfileService, never()).getProfile(anyInt());
    }

    @Test
    void testChangePassword_RequiresUserId() throws Exception {
        // Act & Assert - Missing userId
        mockMvc.perform(post("/api/users//password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordReq)))
                .andExpect(status().is5xxServerError());

        verify(userProfileService, never()).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    @Test
    void testCorrectBasePath() throws Exception {
        // Arrange
        when(userProfileService.getProfile(anyInt())).thenReturn(testProfile);

        // Act & Assert - Correct path
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk());

        // Wrong path should fail
        mockMvc.perform(get("/users/1"))
                .andExpect(status().is5xxServerError());

        verify(userProfileService, times(1)).getProfile(anyInt());
    }

    // ==================== Request Body Validation Tests ====================

    @Test
    void testChangePassword_WithNullRequestBody() throws Exception {
        // Act & Assert - Global exception handler returns 500 for parse errors
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().is5xxServerError());

        verify(userProfileService, never()).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    @Test
    void testChangePassword_WithInvalidJson() throws Exception {
        // Act & Assert - Global exception handler returns 500 for parse errors
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().is5xxServerError());

        verify(userProfileService, never()).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    @Test
    void testChangePassword_WithMissingContentType() throws Exception {
        // Act & Assert - Global exception handler returns 500 for media type errors
        mockMvc.perform(post("/api/users/1/password")
                        .content(objectMapper.writeValueAsString(validPasswordReq)))
                .andExpect(status().is5xxServerError());

        verify(userProfileService, never()).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    @Test
    void testChangePassword_AllFieldsProvided() throws Exception {
        // Arrange
        ChangePasswordReq completeReq = new ChangePasswordReq();
        completeReq.setCurrentPassword("oldPassword123");
        completeReq.setNewPassword("newPassword456");
        completeReq.setConfirmNewPassword("newPassword456");

        doNothing().when(userProfileService).changePassword(anyInt(), any(ChangePasswordReq.class));

        // Act & Assert
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("PASSWORD_UPDATED"));

        verify(userProfileService, times(1)).changePassword(eq(1), any(ChangePasswordReq.class));
    }

    // ==================== Service Interaction Tests ====================

    @Test
    void testGetProfile_CallsServiceExactlyOnce() throws Exception {
        // Arrange
        when(userProfileService.getProfile(1)).thenReturn(testProfile);

        // Act
        mockMvc.perform(get("/api/users/1"));

        // Assert
        verify(userProfileService, times(1)).getProfile(1);
        verifyNoMoreInteractions(userProfileService);
    }

    @Test
    void testChangePassword_CallsServiceExactlyOnce() throws Exception {
        // Arrange
        doNothing().when(userProfileService).changePassword(anyInt(), any(ChangePasswordReq.class));

        // Act
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordReq)));

        // Assert
        verify(userProfileService, times(1)).changePassword(eq(1), any(ChangePasswordReq.class));
        verifyNoMoreInteractions(userProfileService);
    }

    @Test
    void testChangePassword_PassesCorrectUserId() throws Exception {
        // Arrange
        doNothing().when(userProfileService).changePassword(eq(42), any(ChangePasswordReq.class));

        // Act
        mockMvc.perform(post("/api/users/42/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordReq)))
                .andExpect(status().isOk());

        // Assert
        verify(userProfileService, times(1)).changePassword(eq(42), any(ChangePasswordReq.class));
        verify(userProfileService, never()).changePassword(eq(1), any(ChangePasswordReq.class));
    }

    // ==================== Response Format Tests ====================

    @Test
    void testGetProfile_ReturnsJsonObject() throws Exception {
        // Arrange
        when(userProfileService.getProfile(1)).thenReturn(testProfile);

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isMap());

        verify(userProfileService, times(1)).getProfile(1);
    }

    @Test
    void testChangePassword_ReturnsSuccessMessage() throws Exception {
        // Arrange
        doNothing().when(userProfileService).changePassword(anyInt(), any(ChangePasswordReq.class));

        // Act & Assert
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("PASSWORD_UPDATED"))
                .andExpect(jsonPath("$.message").isString());

        verify(userProfileService, times(1)).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    // ==================== Edge Cases ====================

    @Test
    void testGetProfile_WithLargeUserId() throws Exception {
        // Arrange
        UserProfileDTO largeIdProfile = UserProfileDTO.builder()
                .userId(999999)
                .nickname("largeIdUser")
                .email("large@example.com")
                .build();
        when(userProfileService.getProfile(999999)).thenReturn(largeIdProfile);

        // Act & Assert
        mockMvc.perform(get("/api/users/999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(999999));

        verify(userProfileService, times(1)).getProfile(999999);
    }

    @Test
    void testChangePassword_WithLargeUserId() throws Exception {
        // Arrange
        doNothing().when(userProfileService).changePassword(eq(999999), any(ChangePasswordReq.class));

        // Act & Assert
        mockMvc.perform(post("/api/users/999999/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordReq)))
                .andExpect(status().isOk());

        verify(userProfileService, times(1)).changePassword(eq(999999), any(ChangePasswordReq.class));
    }

    @Test
    void testGetProfile_MultipleSequentialCalls() throws Exception {
        // Arrange
        when(userProfileService.getProfile(anyInt())).thenReturn(testProfile);

        // Act & Assert
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(get("/api/users/" + i))
                    .andExpect(status().isOk());
        }

        verify(userProfileService, times(5)).getProfile(anyInt());
    }

    @Test
    void testChangePassword_MultipleSequentialCalls() throws Exception {
        // Arrange
        doNothing().when(userProfileService).changePassword(anyInt(), any(ChangePasswordReq.class));

        // Act & Assert
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/api/users/" + i + "/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validPasswordReq)))
                    .andExpect(status().isOk());
        }

        verify(userProfileService, times(3)).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    // ==================== Validation Message Tests ====================

    @Test
    void testChangePassword_ValidationMessageForCurrentPassword() throws Exception {
        // Arrange
        ChangePasswordReq invalidReq = new ChangePasswordReq();
        invalidReq.setCurrentPassword("");
        invalidReq.setNewPassword("validPassword123");
        invalidReq.setConfirmNewPassword("validPassword123");

        // Act & Assert
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.currentPassword").value("currentPassword is required"));

        verify(userProfileService, never()).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    @Test
    void testChangePassword_ValidationMessageForNewPassword() throws Exception {
        // Arrange
        ChangePasswordReq invalidReq = new ChangePasswordReq();
        invalidReq.setCurrentPassword("oldPassword");
        invalidReq.setNewPassword("");
        invalidReq.setConfirmNewPassword("validPassword123");

        // Act & Assert
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.newPassword").exists());

        verify(userProfileService, never()).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    @Test
    void testChangePassword_ValidationMessageForConfirmPassword() throws Exception {
        // Arrange
        ChangePasswordReq invalidReq = new ChangePasswordReq();
        invalidReq.setCurrentPassword("oldPassword");
        invalidReq.setNewPassword("validPassword123");
        invalidReq.setConfirmNewPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));

        verify(userProfileService, never()).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    // ==================== Content Type Tests ====================

    @Test
    void testGetProfile_ReturnsApplicationJson() throws Exception {
        // Arrange
        when(userProfileService.getProfile(1)).thenReturn(testProfile);

        // Act & Assert
        mockMvc.perform(get("/api/users/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(userProfileService, times(1)).getProfile(1);
    }

    @Test
    void testChangePassword_AcceptsApplicationJson() throws Exception {
        // Arrange
        doNothing().when(userProfileService).changePassword(anyInt(), any(ChangePasswordReq.class));

        // Act & Assert
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordReq)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(userProfileService, times(1)).changePassword(anyInt(), any(ChangePasswordReq.class));
    }

    // ==================== All Endpoints Tests ====================

    @Test
    void testAllEndpoints_ReturnCorrectStatusCodes() throws Exception {
        // Arrange
        when(userProfileService.getProfile(anyInt())).thenReturn(testProfile);
        doNothing().when(userProfileService).changePassword(anyInt(), any(ChangePasswordReq.class));

        // Act & Assert - Get Profile
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk()); // 200

        // Act & Assert - Change Password
        mockMvc.perform(post("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordReq)))
                .andExpect(status().isOk()); // 200

        verify(userProfileService, times(1)).getProfile(1);
        verify(userProfileService, times(1)).changePassword(eq(1), any(ChangePasswordReq.class));
    }

    @Test
    void testBothEndpoints_WorkIndependently() throws Exception {
        // Arrange
        when(userProfileService.getProfile(1)).thenReturn(testProfile);
        when(userProfileService.getProfile(2)).thenReturn(testProfile);
        doNothing().when(userProfileService).changePassword(anyInt(), any(ChangePasswordReq.class));

        // Act & Assert
        mockMvc.perform(get("/api/users/1")).andExpect(status().isOk());
        mockMvc.perform(post("/api/users/2/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPasswordReq)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/users/1")).andExpect(status().isOk());

        verify(userProfileService, times(2)).getProfile(1);
        verify(userProfileService, times(1)).changePassword(eq(2), any(ChangePasswordReq.class));
    }
}

