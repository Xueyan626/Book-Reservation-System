package usyd.library_reservation_system.library_reservation_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import usyd.library_reservation_system.library_reservation_system.dto.*;
import usyd.library_reservation_system.library_reservation_system.service.AuthService_UserLogin;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController_UserLogin
 */
@WebMvcTest(AuthController_UserLogin.class)
class AuthController_UserLoginTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private AuthService_UserLogin authService;

    @Test
    @DisplayName("POST /api/auth/login/request-code -> 202 Accepted + return challengeToken")
    void requestLoginCode_ok() throws Exception {
        // 模拟 service 返回值
        LoginStartResponse mockResp =
                new LoginStartResponse("Verification code sent", "challenge-123");

        Mockito.when(authService.startLogin(any(LoginStartRequest.class)))
                .thenReturn(mockResp);

        // 请求体
        LoginStartRequest req = new LoginStartRequest("user@example.com");

        mvc.perform(post("/api/auth/login/request-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isAccepted())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Verification code sent")))
                .andExpect(jsonPath("$.challengeToken", is("challenge-123")));
    }

    @Test
    @DisplayName("POST /api/auth/login/verify -> 200 OK + return login success info")
    void verify_ok() throws Exception {
        // 模拟 service 返回值
        LoginSuccessResponse mockResp =
                new LoginSuccessResponse("Login successful", 1001, "Sherry", "user@example.com");

        Mockito.when(authService.verifyLogin(any(LoginVerifyRequest.class)))
                .thenReturn(mockResp);

        // 请求体
        LoginVerifyRequest req = new LoginVerifyRequest(
                "challenge-123",
                "123456",
                "user@example.com",
                "password123"
        );

        mvc.perform(post("/api/auth/login/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Login successful")))
                .andExpect(jsonPath("$.userId", is(1001)))
                .andExpect(jsonPath("$.nickname", is("Sherry")))
                .andExpect(jsonPath("$.email", is("user@example.com")));
    }

    @Test
    @DisplayName("POST /api/auth/login/request-code -> 400 Bad Request (invalid email)")
    void requestLoginCode_invalidEmail() throws Exception {
        // Invalid email triggers @Valid validation failure
        LoginStartRequest invalidReq = new LoginStartRequest("not-an-email");

        mvc.perform(post("/api/auth/login/request-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest());
    }
}
