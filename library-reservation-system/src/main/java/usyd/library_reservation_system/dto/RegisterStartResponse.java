package usyd.library_reservation_system.library_reservation_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterStartResponse {
    private String challengeToken;
    private long   expiresAt; // epoch seconds
    private int ttlMinutes;
}

