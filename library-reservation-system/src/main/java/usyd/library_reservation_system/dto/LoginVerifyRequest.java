package usyd.library_reservation_system.library_reservation_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Complete login: challengeToken + code + email + password */
public record LoginVerifyRequest(
        @NotBlank String challengeToken,
        @NotBlank @Size(min = 4, max = 8) String code,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 64) String password
) {}
