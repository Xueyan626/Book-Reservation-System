package usyd.library_reservation_system.library_reservation_system.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import usyd.library_reservation_system.library_reservation_system.dto.AdminPwdLoginReq;
import usyd.library_reservation_system.library_reservation_system.dto.adminlogin.AdminLoginResp;
import usyd.library_reservation_system.library_reservation_system.model.AdminEntity;
import usyd.library_reservation_system.library_reservation_system.repository.AdminRepository;

import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * High-confidence tests for AuthService_AdminLogin.
 * - All collaborators are mocked.
 * - We verify email normalization, login log updates, repository saves, and return values.
 * - Time-dependent string is asserted by shape, not exact moment (robust).
 */
@ExtendWith(MockitoExtension.class)
class AuthService_AdminLoginTest {

    @Mock
    AdminRepository adminRepository;

    @InjectMocks
    AuthService_AdminLogin service;

    // Regex for "yyyy-MM-dd HH:mm:ss <message>"
    private static final Pattern TS_LINE =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} (login (successful|failed))$");

    @Nested
    class PwdLogin {

        @Test
        @DisplayName("pwdLogin: admin not found -> throws IllegalArgumentException('Admin not found')")
        void adminNotFound_throws() {
            // Input email intentionally has spaces and different cases to test normalization
            AdminPwdLoginReq req = new AdminPwdLoginReq();
            req.setEmail("  ADMIN@Example.com ");
            req.setPassword("any");

            when(adminRepository.findByEmailIgnoreCase("admin@example.com"))
                    .thenReturn(Optional.empty());

            IllegalArgumentException ex =
                    assertThrows(IllegalArgumentException.class, () -> service.pwdLogin(req));
            assertEquals("Admin not found", ex.getMessage());

            // Ensure we queried with normalized lower-case trimmed email
            verify(adminRepository).findByEmailIgnoreCase("admin@example.com");
            // No save or logging when not found
            verify(adminRepository, never()).save(any());
        }

        @Test
        @DisplayName("pwdLogin: password mismatch -> write 'login failed' log, save, then throw IllegalArgumentException")
        void passwordMismatch_logsAndThrows() {
            AdminPwdLoginReq req = new AdminPwdLoginReq();
            req.setEmail("Admin@Example.com");
            req.setPassword("Wrong#123");

            // Use a MOCK AdminEntity so we can verify setLoginLog(...) argument
            AdminEntity admin = mock(AdminEntity.class);
            when(admin.getPassword()).thenReturn("Correct#123");
            when(adminRepository.findByEmailIgnoreCase("admin@example.com"))
                    .thenReturn(Optional.of(admin));

            // Execute
            IllegalArgumentException ex =
                    assertThrows(IllegalArgumentException.class, () -> service.pwdLogin(req));
            assertEquals("Invalid email or password", ex.getMessage());

            // Verify a "failed" log line with timestamp prefix
            ArgumentCaptor<String> logArg = ArgumentCaptor.forClass(String.class);
            verify(admin).setLoginLog(logArg.capture());
            assertTrue(TS_LINE.matcher(logArg.getValue()).matches(),
                    "loginLog line should look like 'yyyy-MM-dd HH:mm:ss login failed'");
            assertTrue(logArg.getValue().endsWith("login failed"));

            // Verify save called once
            verify(adminRepository, times(1)).save(admin);
        }

        @Test
        @DisplayName("pwdLogin: success -> write 'login successful' log, save, and return AdminLoginResp")
        void success_logsAndReturnsResp() {
            AdminPwdLoginReq req = new AdminPwdLoginReq();
            req.setEmail("Admin@Example.com");   // mixed case to validate normalization
            req.setPassword("Correct#123");

            // Mock admin
            AdminEntity admin = mock(AdminEntity.class);
            when(admin.getPassword()).thenReturn("Correct#123");
            when(admin.getAdministratorId()).thenReturn(42);

            when(adminRepository.findByEmailIgnoreCase("admin@example.com"))
                    .thenReturn(Optional.of(admin));

            // Execute
            AdminLoginResp resp = service.pwdLogin(req);

            // Response must carry id + success message
            assertNotNull(resp);
            assertEquals(42, resp.adminId());
            assertEquals("successfully login!", resp.message());

            // Verify log line
            ArgumentCaptor<String> logArg = ArgumentCaptor.forClass(String.class);
            verify(admin).setLoginLog(logArg.capture());
            assertTrue(TS_LINE.matcher(logArg.getValue()).matches(),
                    "loginLog line should look like 'yyyy-MM-dd HH:mm:ss login successful'");
            assertTrue(logArg.getValue().endsWith("login successful"));

            // Saved once
            verify(adminRepository, times(1)).save(admin);

            // Ensure lookup used normalized email
            verify(adminRepository).findByEmailIgnoreCase("admin@example.com");
        }
    }
}
