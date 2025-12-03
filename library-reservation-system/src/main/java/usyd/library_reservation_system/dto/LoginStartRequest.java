package usyd.library_reservation_system.library_reservation_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Send verification code: only email required */
public record LoginStartRequest(
        @NotBlank @Email String email
) {}
