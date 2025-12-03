package usyd.library_reservation_system.library_reservation_system.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminActionResponse {
    private String message;   // eg. "password reset to default"
    private Integer userId;
}
