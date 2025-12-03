package usyd.library_reservation_system.library_reservation_system.dto.adminlogin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Admin login - Step 1: Request body for requesting verification code
 * Client only needs to submit admin's email for sending verification code
 */
public record AdminLoginStartReq(
        @NotBlank(message = "email is required")
        @Email(message = "email format is invalid")
        String email
) {}
