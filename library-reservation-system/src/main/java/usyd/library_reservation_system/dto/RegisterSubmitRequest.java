package usyd.library_reservation_system.library_reservation_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterSubmitRequest {
    private String nickname;
    private String email;
    private String telephone;
    private String password;
    private String confirmPassword;
    private String code;
    private String challengeToken;
}


