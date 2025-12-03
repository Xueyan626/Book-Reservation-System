package usyd.library_reservation_system.library_reservation_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChangePasswordReq {
    @NotBlank(message = "currentPassword is required")
    private String currentPassword;

    @NotBlank(message = "newPassword is required")
    @Size(min = 8, max = 64, message = "newPassword length must be 8~64")
    private String newPassword;

    @NotBlank
    private String confirmNewPassword;
}
