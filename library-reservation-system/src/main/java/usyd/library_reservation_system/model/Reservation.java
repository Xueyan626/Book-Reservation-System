package usyd.library_reservation_system.library_reservation_system.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;


    @Data
    @Entity
    @Table(name = "reservation")
    public class Reservation {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer reservationId;

        private LocalDateTime createDate;

        private Byte status; // 0=待分配/排队, 1=已取, 2=已还, 3=取消

        private Integer userId;

        private Integer bookId;

        private LocalDate takeDate;

        private LocalDate returnDate;
}
