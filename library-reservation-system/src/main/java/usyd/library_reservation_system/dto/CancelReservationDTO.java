package usyd.library_reservation_system.library_reservation_system.dto;

import lombok.Data;

@Data
public class CancelReservationDTO {
    private Integer reservationId;
    private Integer userId; // Prevent users from canceling others' reservations
}

