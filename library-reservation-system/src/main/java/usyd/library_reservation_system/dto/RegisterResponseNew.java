package usyd.library_reservation_system.library_reservation_system.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponseNew {
    private Integer userId;
    private String  message;
}
