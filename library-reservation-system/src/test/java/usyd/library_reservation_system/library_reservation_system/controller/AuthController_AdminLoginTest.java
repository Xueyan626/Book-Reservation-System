package usyd.library_reservation_system.library_reservation_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import usyd.library_reservation_system.library_reservation_system.dto.AdminPwdLoginReq;
import usyd.library_reservation_system.library_reservation_system.dto.adminlogin.AdminLoginResp;
import usyd.library_reservation_system.library_reservation_system.service.AuthService_AdminLogin;

import static org.hamcrest.Matchers.emptyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer slice test for AuthController_AdminLogin.
 * Uses @WebMvcTest to load only MVC components and mocks the service layer.
 */
@WebMvcTest(AuthController_AdminLogin.class)
class AuthController_AdminLoginTest {

    @Autowired
    private MockMvc mvc;          // Mocked MVC entry point for performing HTTP requests

    @Autowired
    private ObjectMapper mapper;  // Jackson mapper for serializing DTOs to JSON

    @MockBean
    private AuthService_AdminLogin service; // Mocked service injected into the controller

    @Test
    @DisplayName("POST /api/admin/auth/login -> 200 OK, service is invoked, empty body returned")
    void login_ok_minimal() throws Exception {
        // Arrange
        // Return null from the service to avoid coupling to AdminLoginResp structure
        Mockito.when(service.pwdLogin(ArgumentMatchers.any(AdminPwdLoginReq.class)))
                .thenReturn((AdminLoginResp) null);

        // Build a valid request body
        AdminPwdLoginReq req = new AdminPwdLoginReq();
        req.setEmail("admin@example.com");
        req.setPassword("Secret#123");

        // Act & Assert
        mvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                // Because the mocked service returns null, the controller writes an empty body
                .andExpect(content().string(emptyString()));

        // Verify the service was called with the expected parameters
        Mockito.verify(service).pwdLogin(argThat(r ->
                "admin@example.com".equals(r.getEmail())
                        && "Secret#123".equals(r.getPassword())));
    }

    @Test
    @DisplayName("POST /api/admin/auth/login -> 400 Bad Request when validation fails (@Valid)")
    void login_validation_failed() throws Exception {
        // Arrange
        // Invalid email and blank password should trigger Bean Validation errors
        String badJson = """
        {
          "email": "not-an-email",
          "password": ""
        }
        """;

        // Act & Assert
        mvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());

        // The service must not be called when validation fails at the controller layer
        Mockito.verifyNoInteractions(service);
    }
}
