package usyd.library_reservation_system.library_reservation_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import usyd.library_reservation_system.library_reservation_system.model.Reservation;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    // Find queued users for a book (status=0), sorted by time
    List<Reservation> findByBookIdAndStatusOrderByCreateDateAsc(Integer bookId, Byte status);
    List<Reservation> findByStatus(Byte status);
    // Find user's subscription list
    List<Reservation> findByUserIdOrderByCreateDateDesc(Integer userId);
}

