package usyd.library_reservation_system.library_reservation_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import usyd.library_reservation_system.library_reservation_system.dto.*;
import usyd.library_reservation_system.library_reservation_system.service.AuthService;
import usyd.library_reservation_system.library_reservation_system.service.AuthService_UserRegister;

import static org.hamcrest.Matchers.emptyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer slice tests for AuthController.
 * - Loads only MVC beans for the controller under test.
 * - Mocks the service layer to keep tests stable and fast.
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private AuthService_UserRegister userRegisterService;

    // ---------- /api/auth/register (legacy register with @Valid) ----------

    @Test
    @DisplayName("POST /api/auth/register -> 400 Bad Request when payload fails @Valid")
    void register_invalid_returns400() throws Exception {
        // Minimal clearly-invalid JSON to trigger Bean Validation
        String badJson = """
        {
          "email": "not-an-email"
        }
        """;

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }

    // If you want a 201 success test as well, see the note at the bottom.

    // ---------- /api/auth/register/start ----------

    @Test
    @DisplayName("POST /api/auth/register/start -> 200 OK and delegates to userRegisterService.startRegister")
    void startRegister_ok() throws Exception {
        // Prepare a minimal, plausible request payload for start
        // (Typically: { "email": "user@example.com" })
        String reqJson = """
        {
          "email": "user@example.com"
        }
        """;

        // We don't assert body structure to keep it decoupled; returning null is fine
        when(userRegisterService.startRegister(any(RegisterStartRequest.class)))
                .thenReturn((RegisterStartResponse) null);

        mvc.perform(post("/api/auth/register/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson))
                .andExpect(status().isOk())
                .andExpect(content().string(emptyString())); // null body -> empty response

        // Verify service delegation occurred
        verify(userRegisterService).startRegister(ArgumentMatchers.any(RegisterStartRequest.class));
    }

    // ---------- /api/auth/register/submit ----------

    @Test
    @DisplayName("POST /api/auth/register/submit -> 200 OK and delegates to userRegisterService.submitRegister")
    void submitRegister_ok() throws Exception {
        // Prepare a plausible submit payload.
        // Your real RegisterSubmitRequest likely includes: challengeToken, email, code, password, confirmPassword, nickname, telephone.
        String reqJson = """
        {
          "challengeToken": "jwt-abc",
          "email": "user@example.com",
          "code": "123456",
          "password": "Secret#123",
          "confirmPassword": "Secret#123",
          "nickname": "Sherry",
          "telephone": "0400000000"
        }
        """;

        // Return a null to avoid coupling to UserProfileDTO fields/constructors
        when(userRegisterService.submitRegister(any(RegisterSubmitRequest.class)))
                .thenReturn((UserProfileDTO) null);

        mvc.perform(post("/api/auth/register/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson))
                .andExpect(status().isOk())
                .andExpect(content().string(emptyString()));

        // Verify service delegation
        verify(userRegisterService).submitRegister(any(RegisterSubmitRequest.class));
    }
}
