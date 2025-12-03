package usyd.library_reservation_system.library_reservation_system.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterStartRequest {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String nickname;
    @NotBlank private String telephone;
}

