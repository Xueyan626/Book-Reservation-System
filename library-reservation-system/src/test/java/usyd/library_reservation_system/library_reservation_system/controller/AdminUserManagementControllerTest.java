package usyd.library_reservation_system.library_reservation_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import usyd.library_reservation_system.library_reservation_system.dto.AdminActionResponse;
import usyd.library_reservation_system.library_reservation_system.dto.AdminUserSummaryDTO;
import usyd.library_reservation_system.library_reservation_system.service.AdminUserManagementService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserManagementController.class)
class AdminUserManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminUserManagementService adminUserManagementService;

    @Autowired
    private ObjectMapper objectMapper;

    private AdminUserSummaryDTO testUser;
    private AdminActionResponse successResponse;

    @BeforeEach
    void setUp() {
        testUser = AdminUserSummaryDTO.builder()
                .userId(1)
                .nickname("testUser")
                .email("test@example.com")
                .telephone("0412345678")
                .isActive(true)
                .build();

        successResponse = AdminActionResponse.builder()
                .message("Operation successful")
                .userId(1)
                .build();
    }

    // ==================== Search Tests ====================

    @Test
    void testSearch_WithMatchingUsers() throws Exception {
        // Arrange
        AdminUserSummaryDTO user1 = AdminUserSummaryDTO.builder()
                .userId(1)
                .nickname("alice")
                .email("alice@example.com")
                .isActive(true)
                .build();

        AdminUserSummaryDTO user2 = AdminUserSummaryDTO.builder()
                .userId(2)
                .nickname("alicia")
                .email("alicia@example.com")
                .isActive(true)
                .build();

        List<AdminUserSummaryDTO> users = Arrays.asList(user1, user2);
        when(adminUserManagementService.searchByNickname("ali")).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/admin/users/search")
                        .param("username", "ali"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nickname").value("alice"))
                .andExpect(jsonPath("$[1].nickname").value("alicia"));

        verify(adminUserManagementService, times(1)).searchByNickname("ali");
    }

    @Test
    void testSearch_WithNoMatchingUsers() throws Exception {
        // Arrange
        when(adminUserManagementService.searchByNickname("nonexistent")).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/admin/users/search")
                        .param("username", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(adminUserManagementService, times(1)).searchByNickname("nonexistent");
    }

    @Test
    void testSearch_WithSingleUser() throws Exception {
        // Arrange
        List<AdminUserSummaryDTO> users = Arrays.asList(testUser);
        when(adminUserManagementService.searchByNickname("testUser")).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/admin/users/search")
                        .param("username", "testUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].nickname").value("testUser"))
                .andExpect(jsonPath("$[0].isActive").value(true));

        verify(adminUserManagementService, times(1)).searchByNickname("testUser");
    }

    @Test
    void testSearch_WithEmptyString() throws Exception {
        // Arrange
        List<AdminUserSummaryDTO> allUsers = Arrays.asList(testUser);
        when(adminUserManagementService.searchByNickname("")).thenReturn(allUsers);

        // Act & Assert
        mockMvc.perform(get("/api/admin/users/search")
                        .param("username", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(adminUserManagementService, times(1)).searchByNickname("");
    }

    @Test
    void testSearch_WithSpecialCharacters() throws Exception {
        // Arrange
        when(adminUserManagementService.searchByNickname("user@123")).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/admin/users/search")
                        .param("username", "user@123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(adminUserManagementService, times(1)).searchByNickname("user@123");
    }

    @Test
    void testSearch_WithBannedUsers() throws Exception {
        // Arrange
        AdminUserSummaryDTO bannedUser = AdminUserSummaryDTO.builder()
                .userId(3)
                .nickname("bannedUser")
                .email("banned@example.com")
                .isActive(false)
                .build();

        List<AdminUserSummaryDTO> users = Arrays.asList(bannedUser);
        when(adminUserManagementService.searchByNickname("banned")).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/admin/users/search")
                        .param("username", "banned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isActive").value(false));

        verify(adminUserManagementService, times(1)).searchByNickname("banned");
    }

    // ==================== Reset Password Tests ====================

    @Test
    void testResetPassword_Success() throws Exception {
        // Arrange
        AdminActionResponse response = new AdminActionResponse();
        response.setMessage("Password reset successful");
        when(adminUserManagementService.resetPasswordToDefault(eq(1), anyString())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/admin/users/1/reset-password"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Password reset successful"));

        verify(adminUserManagementService, times(1)).resetPasswordToDefault(eq(1), eq("123456789"));
    }

    @Test
    void testResetPassword_WithDifferentUserId() throws Exception {
        // Arrange
        AdminActionResponse response = new AdminActionResponse();
        response.setMessage("Password reset successful");
        when(adminUserManagementService.resetPasswordToDefault(eq(99), anyString())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/admin/users/99/reset-password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successful"));

        verify(adminUserManagementService, times(1)).resetPasswordToDefault(eq(99), eq("123456789"));
    }

    @Test
    void testResetPassword_ServiceThrowsException() throws Exception {
        // Arrange
        when(adminUserManagementService.resetPasswordToDefault(eq(999), anyString()))
                .thenThrow(new IllegalArgumentException("User not found"));

        // Act & Assert
        mockMvc.perform(post("/api/admin/users/999/reset-password"))
                .andExpect(status().isBadRequest());

        verify(adminUserManagementService, times(1)).resetPasswordToDefault(eq(999), eq("123456789"));
    }

    @Test
    void testResetPassword_UsesDefaultPassword() throws Exception {
        // Arrange
        AdminActionResponse response = new AdminActionResponse();
        response.setMessage("Password reset to default");
        when(adminUserManagementService.resetPasswordToDefault(anyInt(), eq("123456789"))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/admin/users/5/reset-password"))
                .andExpect(status().isOk());

        verify(adminUserManagementService, times(1)).resetPasswordToDefault(eq(5), eq("123456789"));
    }

    @Test
    void testResetPassword_MultipleUsers() throws Exception {
        // Arrange
        AdminActionResponse response = new AdminActionResponse();
        response.setMessage("Password reset successful");
        when(adminUserManagementService.resetPasswordToDefault(anyInt(), anyString())).thenReturn(response);

        // Act & Assert
        for (int userId = 1; userId <= 3; userId++) {
            mockMvc.perform(post("/api/admin/users/" + userId + "/reset-password"))
                    .andExpect(status().isOk());
        }

        verify(adminUserManagementService, times(3)).resetPasswordToDefault(anyInt(), eq("123456789"));
    }

    // ==================== Ban User Tests ====================

    @Test
    void testBanUser_Success() throws Exception {
        // Arrange
        doNothing().when(adminUserManagementService).banUser(1);

        // Act & Assert
        mockMvc.perform(patch("/api/admin/users/1/ban"))
                .andExpect(status().isNoContent());

        verify(adminUserManagementService, times(1)).banUser(1);
    }

    @Test
    void testBanUser_WithDifferentUserId() throws Exception {
        // Arrange
        doNothing().when(adminUserManagementService).banUser(5);

        // Act & Assert
        mockMvc.perform(patch("/api/admin/users/5/ban"))
                .andExpect(status().isNoContent());

        verify(adminUserManagementService, times(1)).banUser(5);
    }

    @Test
    void testBanUser_ServiceThrowsException() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("User not found"))
                .when(adminUserManagementService).banUser(999);

        // Act & Assert
        mockMvc.perform(patch("/api/admin/users/999/ban"))
                .andExpect(status().isBadRequest());

        verify(adminUserManagementService, times(1)).banUser(999);
    }

    @Test
    void testBanUser_MultipleUsers() throws Exception {
        // Arrange
        doNothing().when(adminUserManagementService).banUser(anyInt());

        // Act & Assert
        for (int userId = 1; userId <= 5; userId++) {
            mockMvc.perform(patch("/api/admin/users/" + userId + "/ban"))
                    .andExpect(status().isNoContent());
        }

        verify(adminUserManagementService, times(5)).banUser(anyInt());
    }

    @Test
    void testBanUser_AlreadyBanned() throws Exception {
        // Arrange
        doThrow(new IllegalStateException("User already banned"))
                .when(adminUserManagementService).banUser(3);

        // Act & Assert
        mockMvc.perform(patch("/api/admin/users/3/ban"))
                .andExpect(status().is5xxServerError());

        verify(adminUserManagementService, times(1)).banUser(3);
    }

    // ==================== Unban User Tests ====================

    @Test
    void testUnbanUser_Success() throws Exception {
        // Arrange
        doNothing().when(adminUserManagementService).unbanUser(1);

        // Act & Assert
        mockMvc.perform(patch("/api/admin/users/1/unban"))
                .andExpect(status().isNoContent());

        verify(adminUserManagementService, times(1)).unbanUser(1);
    }

    @Test
    void testUnbanUser_WithDifferentUserId() throws Exception {
        // Arrange
        doNothing().when(adminUserManagementService).unbanUser(10);

        // Act & Assert
        mockMvc.perform(patch("/api/admin/users/10/unban"))
                .andExpect(status().isNoContent());

        verify(adminUserManagementService, times(1)).unbanUser(10);
    }

    @Test
    void testUnbanUser_ServiceThrowsException() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("User not found"))
                .when(adminUserManagementService).unbanUser(999);

        // Act & Assert
        mockMvc.perform(patch("/api/admin/users/999/unban"))
                .andExpect(status().isBadRequest());

        verify(adminUserManagementService, times(1)).unbanUser(999);
    }

    @Test
    void testUnbanUser_MultipleUsers() throws Exception {
        // Arrange
        doNothing().when(adminUserManagementService).unbanUser(anyInt());

        // Act & Assert
        for (int userId = 1; userId <= 5; userId++) {
            mockMvc.perform(patch("/api/admin/users/" + userId + "/unban"))
                    .andExpect(status().isNoContent());
        }

        verify(adminUserManagementService, times(5)).unbanUser(anyInt());
    }

    @Test
    void testUnbanUser_NotBanned() throws Exception {
        // Arrange
        doThrow(new IllegalStateException("User is not banned"))
                .when(adminUserManagementService).unbanUser(2);

        // Act & Assert
        mockMvc.perform(patch("/api/admin/users/2/unban"))
                .andExpect(status().is5xxServerError());

        verify(adminUserManagementService, times(1)).unbanUser(2);
    }

    // ==================== Integration Tests ====================

    @Test
    void testSearchAndBan_Workflow() throws Exception {
        // Arrange
        List<AdminUserSummaryDTO> users = Arrays.asList(testUser);
        when(adminUserManagementService.searchByNickname("testUser")).thenReturn(users);
        doNothing().when(adminUserManagementService).banUser(1);

        // Act & Assert - Search
        mockMvc.perform(get("/api/admin/users/search")
                        .param("username", "testUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1));

        // Act & Assert - Ban
        mockMvc.perform(patch("/api/admin/users/1/ban"))
                .andExpect(status().isNoContent());

        verify(adminUserManagementService, times(1)).searchByNickname("testUser");
        verify(adminUserManagementService, times(1)).banUser(1);
    }

    @Test
    void testBanAndUnban_Workflow() throws Exception {
        // Arrange
        doNothing().when(adminUserManagementService).banUser(1);
        doNothing().when(adminUserManagementService).unbanUser(1);

        // Act & Assert - Ban
        mockMvc.perform(patch("/api/admin/users/1/ban"))
                .andExpect(status().isNoContent());

        // Act & Assert - Unban
        mockMvc.perform(patch("/api/admin/users/1/unban"))
                .andExpect(status().isNoContent());

        verify(adminUserManagementService, times(1)).banUser(1);
        verify(adminUserManagementService, times(1)).unbanUser(1);
    }

    @Test
    void testSearchAndResetPassword_Workflow() throws Exception {
        // Arrange
        List<AdminUserSummaryDTO> users = Arrays.asList(testUser);
        when(adminUserManagementService.searchByNickname("testUser")).thenReturn(users);
        when(adminUserManagementService.resetPasswordToDefault(eq(1), anyString())).thenReturn(successResponse);

        // Act & Assert - Search
        mockMvc.perform(get("/api/admin/users/search")
                        .param("username", "testUser"))
                .andExpect(status().isOk());

        // Act & Assert - Reset Password
        mockMvc.perform(post("/api/admin/users/1/reset-password"))
                .andExpect(status().isOk());

        verify(adminUserManagementService, times(1)).searchByNickname("testUser");
        verify(adminUserManagementService, times(1)).resetPasswordToDefault(eq(1), eq("123456789"));
    }

    // ==================== Edge Cases ====================

    @Test
    void testSearch_WithCaseInsensitiveMatch() throws Exception {
        // Arrange
        List<AdminUserSummaryDTO> users = Arrays.asList(testUser);
        when(adminUserManagementService.searchByNickname("TESTUSER")).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/admin/users/search")
                        .param("username", "TESTUSER"))
                .andExpect(status().isOk());

        verify(adminUserManagementService, times(1)).searchByNickname("TESTUSER");
    }

    @Test
    void testSearch_WithWhitespace() throws Exception {
        // Arrange
        when(adminUserManagementService.searchByNickname(" test ")).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/admin/users/search")
                        .param("username", " test "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(adminUserManagementService, times(1)).searchByNickname(" test ");
    }

    @Test
    void testResetPassword_WithZeroUserId() throws Exception {
        // Arrange
        when(adminUserManagementService.resetPasswordToDefault(eq(0), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid user ID"));

        // Act & Assert
        mockMvc.perform(post("/api/admin/users/0/reset-password"))
                .andExpect(status().isBadRequest());

        verify(adminUserManagementService, times(1)).resetPasswordToDefault(eq(0), eq("123456789"));
    }

    @Test
    void testResetPassword_WithNegativeUserId() throws Exception {
        // Arrange
        when(adminUserManagementService.resetPasswordToDefault(eq(-1), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid user ID"));

        // Act & Assert
        mockMvc.perform(post("/api/admin/users/-1/reset-password"))
                .andExpect(status().isBadRequest());

        verify(adminUserManagementService, times(1)).resetPasswordToDefault(eq(-1), eq("123456789"));
    }

    @Test
    void testBanUser_WithZeroUserId() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid user ID"))
                .when(adminUserManagementService).banUser(0);

        // Act & Assert
        mockMvc.perform(patch("/api/admin/users/0/ban"))
                .andExpect(status().isBadRequest());

        verify(adminUserManagementService, times(1)).banUser(0);
    }

    @Test
    void testUnbanUser_WithZeroUserId() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid user ID"))
                .when(adminUserManagementService).unbanUser(0);

        // Act & Assert
        mockMvc.perform(patch("/api/admin/users/0/unban"))
                .andExpect(status().isBadRequest());

        verify(adminUserManagementService, times(1)).unbanUser(0);
    }

    // ==================== CORS Tests ====================

    @Test
    void testCorsEnabled_ForSearchEndpoint() throws Exception {
        // Arrange
        when(adminUserManagementService.searchByNickname(anyString())).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/admin/users/search")
                        .param("username", "test")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk());

        verify(adminUserManagementService, times(1)).searchByNickname("test");
    }

    @Test
    void testCorsEnabled_ForBanEndpoint() throws Exception {
        // Arrange
        doNothing().when(adminUserManagementService).banUser(anyInt());

        // Act & Assert
        mockMvc.perform(patch("/api/admin/users/1/ban")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isNoContent());

        verify(adminUserManagementService, times(1)).banUser(1);
    }

    // ==================== HTTP Method Tests ====================

    @Test
    void testSearch_OnlyAcceptsGetMethod() throws Exception {
        // Act & Assert - POST should not be allowed (returns 500 due to global exception handler)
        mockMvc.perform(post("/api/admin/users/search")
                        .param("username", "test"))
                .andExpect(status().is5xxServerError());

        verify(adminUserManagementService, never()).searchByNickname(anyString());
    }

    @Test
    void testResetPassword_OnlyAcceptsPostMethod() throws Exception {
        // Act & Assert - GET should not be allowed (returns 500 due to global exception handler)
        mockMvc.perform(get("/api/admin/users/1/reset-password"))
                .andExpect(status().is5xxServerError());

        verify(adminUserManagementService, never()).resetPasswordToDefault(anyInt(), anyString());
    }

    @Test
    void testBan_OnlyAcceptsPatchMethod() throws Exception {
        // Act & Assert - POST should not be allowed (returns 500 due to global exception handler)
        mockMvc.perform(post("/api/admin/users/1/ban"))
                .andExpect(status().is5xxServerError());

        verify(adminUserManagementService, never()).banUser(anyInt());
    }

    @Test
    void testUnban_OnlyAcceptsPatchMethod() throws Exception {
        // Act & Assert - POST should not be allowed (returns 500 due to global exception handler)
        mockMvc.perform(post("/api/admin/users/1/unban"))
                .andExpect(status().is5xxServerError());

        verify(adminUserManagementService, never()).unbanUser(anyInt());
    }

    // ==================== URL Path Tests ====================

    @Test
    void testCorrectBasePath() throws Exception {
        // Arrange
        when(adminUserManagementService.searchByNickname(anyString())).thenReturn(Collections.emptyList());

        // Act & Assert - Correct path
        mockMvc.perform(get("/api/admin/users/search")
                        .param("username", "test"))
                .andExpect(status().isOk());

        // Wrong path should fail (returns 500 due to global exception handler)
        mockMvc.perform(get("/admin/users/search")
                        .param("username", "test"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testResetPassword_RequiresUserId() throws Exception {
        // Act & Assert - Missing userId in path (returns 500 due to global exception handler)
        mockMvc.perform(post("/api/admin/users//reset-password"))
                .andExpect(status().is5xxServerError());

        verify(adminUserManagementService, never()).resetPasswordToDefault(anyInt(), anyString());
    }

    // ==================== Response Content Tests ====================

    @Test
    void testSearch_ReturnsJsonArray() throws Exception {
        // Arrange
        when(adminUserManagementService.searchByNickname(anyString())).thenReturn(Arrays.asList(testUser));

        // Act & Assert
        mockMvc.perform(get("/api/admin/users/search")
                        .param("username", "test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        verify(adminUserManagementService, times(1)).searchByNickname("test");
    }

    @Test
    void testResetPassword_ReturnsJsonObject() throws Exception {
        // Arrange
        when(adminUserManagementService.resetPasswordToDefault(anyInt(), anyString())).thenReturn(successResponse);

        // Act & Assert
        mockMvc.perform(post("/api/admin/users/1/reset-password"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isMap());

        verify(adminUserManagementService, times(1)).resetPasswordToDefault(anyInt(), anyString());
    }

    @Test
    void testBan_ReturnsNoContent() throws Exception {
        // Arrange
        doNothing().when(adminUserManagementService).banUser(anyInt());

        // Act & Assert
        mockMvc.perform(patch("/api/admin/users/1/ban"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(adminUserManagementService, times(1)).banUser(1);
    }

    @Test
    void testUnban_ReturnsNoContent() throws Exception {
        // Arrange
        doNothing().when(adminUserManagementService).unbanUser(anyInt());

        // Act & Assert
        mockMvc.perform(patch("/api/admin/users/1/unban"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(adminUserManagementService, times(1)).unbanUser(1);
    }

    // ==================== Service Interaction Tests ====================

    @Test
    void testServiceIsCalled_ExactlyOncePerRequest() throws Exception {
        // Arrange
        when(adminUserManagementService.searchByNickname(anyString())).thenReturn(Collections.emptyList());
        doNothing().when(adminUserManagementService).banUser(anyInt());
        doNothing().when(adminUserManagementService).unbanUser(anyInt());
        when(adminUserManagementService.resetPasswordToDefault(anyInt(), anyString())).thenReturn(successResponse);

        // Act
        mockMvc.perform(get("/api/admin/users/search").param("username", "test"));
        mockMvc.perform(post("/api/admin/users/1/reset-password"));
        mockMvc.perform(patch("/api/admin/users/1/ban"));
        mockMvc.perform(patch("/api/admin/users/1/unban"));

        // Assert
        verify(adminUserManagementService, times(1)).searchByNickname("test");
        verify(adminUserManagementService, times(1)).resetPasswordToDefault(1, "123456789");
        verify(adminUserManagementService, times(1)).banUser(1);
        verify(adminUserManagementService, times(1)).unbanUser(1);
    }

    @Test
    void testAllEndpoints_ReturnCorrectStatusCodes() throws Exception {
        // Arrange
        when(adminUserManagementService.searchByNickname(anyString())).thenReturn(Collections.emptyList());
        when(adminUserManagementService.resetPasswordToDefault(anyInt(), anyString())).thenReturn(successResponse);
        doNothing().when(adminUserManagementService).banUser(anyInt());
        doNothing().when(adminUserManagementService).unbanUser(anyInt());

        // Act & Assert
        mockMvc.perform(get("/api/admin/users/search").param("username", "test"))
                .andExpect(status().isOk()); // 200

        mockMvc.perform(post("/api/admin/users/1/reset-password"))
                .andExpect(status().isOk()); // 200

        mockMvc.perform(patch("/api/admin/users/1/ban"))
                .andExpect(status().isNoContent()); // 204

        mockMvc.perform(patch("/api/admin/users/1/unban"))
                .andExpect(status().isNoContent()); // 204
    }

    // ==================== Parameter Validation Tests ====================

    @Test
    void testSearch_WithChineseCharacters() throws Exception {
        // Arrange
        AdminUserSummaryDTO chineseUser = AdminUserSummaryDTO.builder()
                .userId(1)
                .nickname("测试用户")
                .email("chinese@example.com")
                .isActive(true)
                .build();

        when(adminUserManagementService.searchByNickname("测试")).thenReturn(Arrays.asList(chineseUser));

        // Act & Assert
        mockMvc.perform(get("/api/admin/users/search")
                        .param("username", "测试")
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nickname").value("测试用户"));

        verify(adminUserManagementService, times(1)).searchByNickname("测试");
    }

    @Test
    void testSearch_WithNumericUsername() throws Exception {
        // Arrange
        when(adminUserManagementService.searchByNickname("12345")).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/admin/users/search")
                        .param("username", "12345"))
                .andExpect(status().isOk());

        verify(adminUserManagementService, times(1)).searchByNickname("12345");
    }

    @Test
    void testBan_WithLargeUserId() throws Exception {
        // Arrange
        doNothing().when(adminUserManagementService).banUser(999999);

        // Act & Assert
        mockMvc.perform(patch("/api/admin/users/999999/ban"))
                .andExpect(status().isNoContent());

        verify(adminUserManagementService, times(1)).banUser(999999);
    }

    @Test
    void testUnban_WithLargeUserId() throws Exception {
        // Arrange
        doNothing().when(adminUserManagementService).unbanUser(999999);

        // Act & Assert
        mockMvc.perform(patch("/api/admin/users/999999/unban"))
                .andExpect(status().isNoContent());

        verify(adminUserManagementService, times(1)).unbanUser(999999);
    }
}

