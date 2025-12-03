package usyd.library_reservation_system.library_reservation_system.dto.adminlogin;

import jakarta.validation.constraints.NotBlank;

/**
 * Admin login - Step 2: Request body for submitting verification code + password
 * Client needs to send back the challengeToken from step 1; and input the verification code received via email, as well as the plaintext password from DB
 */
public record AdminLoginVerifyReq(
        @NotBlank(message = "challengeToken is required")
        String challengeToken,

        @NotBlank(message = "code is required")
        String code,

        @NotBlank(message = "password is required")
        String password
) {}
