package usyd.library_reservation_system.library_reservation_system.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Size(max = 50) String nickname,
        @NotBlank @Pattern(regexp = "^[0-9+\\- ]{6,20}$", message = "telephone format invalid")
        String telephone,
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(min = 8, max = 64) String password
) {}
