package usyd.library_reservation_system.library_reservation_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminReservationDTO {
    private Integer reservationId;
    private Integer bookId;  // Added bookId field
    private String bookName;
    private String userNickname;
    private LocalDateTime createDate;
    private LocalDate takeDate;
    private LocalDate returnDate;
    private Byte status; // 0=Queuing, 1=Assigned, 2=Returned, 3=Cancelled
}

