package usyd.library_reservation_system.library_reservation_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReservationResponseDTO {
    private String message;
    private Byte status; // 0=Pending assignment/Queuing, 1=Picked up, 2=Returned, 3=Cancelled
}

