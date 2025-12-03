package usyd.library_reservation_system.library_reservation_system.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProfileDTO {
    private Integer userId;
    private String nickname;
    private String email;
    private String telephone;
    private String registrationTime; // Optional: return null if column doesn't exist
}
