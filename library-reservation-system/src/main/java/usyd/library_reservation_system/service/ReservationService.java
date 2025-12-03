package usyd.library_reservation_system.library_reservation_system.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usyd.library_reservation_system.library_reservation_system.dto.AdminReservationDTO;
import usyd.library_reservation_system.library_reservation_system.dto.ReservationResponseDTO;
import usyd.library_reservation_system.library_reservation_system.model.Book;
import usyd.library_reservation_system.library_reservation_system.model.Reservation;
import usyd.library_reservation_system.library_reservation_system.model.UserEntity;
import usyd.library_reservation_system.library_reservation_system.repository.BookRepository;
import usyd.library_reservation_system.library_reservation_system.repository.ReservationRepository;
import usyd.library_reservation_system.library_reservation_system.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final BookRepository bookRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public ReservationService(BookRepository bookRepository,
                              ReservationRepository reservationRepository,
                              UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
    }

    /**
     * User reserves a book
     */
    @Transactional
    public ReservationResponseDTO reserveBook(Integer userId, Integer bookId) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return new ReservationResponseDTO("User not found", (byte) -1);
        }

        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isEmpty()) {
            return new ReservationResponseDTO("Book not found", (byte) -1);
        }

        Book book = bookOpt.get();

        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setBookId(bookId);
        reservation.setCreateDate(LocalDateTime.now());

        if (book.getQuantity() > 0) {
            // Has stock
            reservation.setStatus((byte) 1); // Assigned/Available for pickup
            book.setQuantity(book.getQuantity() - 1);
            book.setNumReservation(book.getNumReservation() + 1);
            bookRepository.save(book);
        } else {
            // No stock, queuing
            reservation.setStatus((byte) 0); // Queuing
            book.setNumReservation(book.getNumReservation() + 1);
            bookRepository.save(book);
        }

        reservationRepository.save(reservation);

        return new ReservationResponseDTO("Reservation successful", reservation.getStatus());
    }

    @Transactional
    public ReservationResponseDTO pickupBook(Integer reservationId) {
        Optional<Reservation> resOpt = reservationRepository.findById(reservationId);
        if (resOpt.isEmpty()) {
            return new ReservationResponseDTO("Reservation not found", (byte) -1);
        }

        Reservation reservation = resOpt.get();

        // Only status=1 can be changed to picked up
        if (reservation.getStatus() != 1) {
            return new ReservationResponseDTO("Reservation is not in available for pickup state", reservation.getStatus());
        }

        reservation.setStatus((byte) 4); // Picked up
        reservation.setTakeDate(LocalDate.now());
        reservationRepository.save(reservation);

        return new ReservationResponseDTO("User has picked up the book, status updated to picked up", (byte) 4);
    }

    @Transactional
    public ReservationResponseDTO cancelReservation(Integer userId, Integer reservationId) {
        Optional<Reservation> resOpt = reservationRepository.findById(reservationId);
        if (resOpt.isEmpty()) {
            return new ReservationResponseDTO("Reservation not found", (byte) -1);
        }

        Reservation reservation = resOpt.get();

        // Verify if it's the current user's reservation
        if (!reservation.getUserId().equals(userId)) {
            return new ReservationResponseDTO("No permission to cancel others' reservation", (byte) -1);
        }

        // Already cancelled or returned, no repeat operation allowed
        if (reservation.getStatus() == 2 || reservation.getStatus() == 3) {
            return new ReservationResponseDTO("Reservation is already completed or cancelled", reservation.getStatus());
        }

        // Update status to cancelled
        Byte oldStatus = reservation.getStatus();
        reservation.setStatus((byte) 3); // 3=Cancelled
        reservationRepository.save(reservation);

        // If previously assigned, need to return stock
        if (oldStatus == 1) {
            Optional<Book> bookOpt = bookRepository.findById(reservation.getBookId());
            if (bookOpt.isPresent()) {
                Book book = bookOpt.get();
                book.setQuantity(book.getQuantity() + 1);
                book.setNumReservation(Math.max(0, book.getNumReservation() - 1));
                bookRepository.save(book);

                // TODO: Auto-assignment logic can be triggered here, assign to next queuing user
                // Trigger auto assignment (if someone is queuing)
                ReservationResponseDTO autoAssignResult = autoAssignNextUser(book.getBookId());
                if (autoAssignResult.getStatus() == 1) {
                    return new ReservationResponseDTO(
                            "Return successful, automatically assigned to next user: " + autoAssignResult.getMessage(),
                            (byte) 2
                    );
                }
            }
        }

        return new ReservationResponseDTO("Cancellation successful", reservation.getStatus());
    }

    @Transactional
    public ReservationResponseDTO autoAssignNextUser(Integer bookId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isEmpty()) {
            return new ReservationResponseDTO("Book not found", (byte) -1);
        }
        Book book = bookOpt.get();

        if (book.getQuantity() <= 0) {
            return new ReservationResponseDTO("Insufficient stock, unable to assign", (byte) -1);
        }

        // Find earliest queuing user
        List<Reservation> waitingList = reservationRepository.findByBookIdAndStatusOrderByCreateDateAsc(bookId, (byte) 0);

        if (waitingList.isEmpty()) {
            return new ReservationResponseDTO("No users in queue", (byte) -1);
        }

        Reservation nextReservation = waitingList.get(0);

        // Update status to assigned
        nextReservation.setStatus((byte) 1);
        reservationRepository.save(nextReservation);

        // Decrement stock
        book.setQuantity(book.getQuantity() - 1);
        bookRepository.save(book);

        return new ReservationResponseDTO("Assigned to user ID: " + nextReservation.getUserId(), (byte) 1);
    }

    @Transactional
    public ReservationResponseDTO returnBook(Integer reservationId) {
        System.out.println("🔙 ReturnBook called for reservationId: " + reservationId);
        
        Optional<Reservation> resOpt = reservationRepository.findById(reservationId);
        if (resOpt.isEmpty()) {
            System.out.println("🔙 Reservation not found: " + reservationId);
            return new ReservationResponseDTO("Reservation not found", (byte) -1);
        }

        Reservation reservation = resOpt.get();
        System.out.println("🔙 Found reservation: " + reservation.getReservationId() + ", status: " + reservation.getStatus());

        // Only borrowed status (4) allows return
        if (reservation.getStatus() != 4) {
            System.out.println("🔙 Reservation not in returnable state: " + reservation.getStatus());
            return new ReservationResponseDTO("Reservation is not in returnable state", reservation.getStatus());
        }

        // Update status to returned
        reservation.setStatus((byte) 2); // Returned
        reservation.setReturnDate(LocalDate.now());
        reservationRepository.save(reservation);
        System.out.println("🔙 Reservation status updated to 2 (returned)");

        // Return one copy to stock
        Optional<Book> bookOpt = bookRepository.findById(reservation.getBookId());
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.setQuantity(book.getQuantity() + 1);
            bookRepository.save(book);

            // Trigger auto assignment (if someone is queuing)
            ReservationResponseDTO autoAssignResult = autoAssignNextUser(book.getBookId());
            if (autoAssignResult.getStatus() == 1) {
                return new ReservationResponseDTO(
                        "Return successful, automatically assigned to next user: " + autoAssignResult.getMessage(),
                        (byte) 2
                );
            }
        }

        return new ReservationResponseDTO("Return successful", (byte) 2);
    }

    @Transactional(readOnly = true)
    public List<AdminReservationDTO> getAllReservations(Byte status) {
        List<Reservation> reservations = reservationRepository.findAll();

        if (status == null) {
            reservations = reservationRepository.findAll();
        } else {
            reservations = reservationRepository.findByStatus(status);
        }

        return reservations.stream().map(res -> {
            String bookName = bookRepository.findById(res.getBookId())
                    .map(Book::getBookName)
                    .orElse("Unknown Book");

            String userNickname = userRepository.findById(res.getUserId())
                    .map(UserEntity::getNickname)
                    .orElse("Unknown User");

            return new AdminReservationDTO(
                    res.getReservationId(),
                    res.getBookId(),  // Add bookId
                    bookName,
                    userNickname,
                    res.getCreateDate(),
                    res.getTakeDate(),
                    res.getReturnDate(),
                    res.getStatus()
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public ReservationResponseDTO approveTakeBook(Integer reservationId) {
        Optional<Reservation> resOpt = reservationRepository.findById(reservationId);
        if (resOpt.isEmpty()) {
            return new ReservationResponseDTO("Reservation not found", (byte) -1);
        }

        Reservation reservation = resOpt.get();

        // Only status=1 can be changed to picked up
        if (reservation.getStatus() != 1) {
            return new ReservationResponseDTO("Reservation is not in available for pickup state", reservation.getStatus());
        }

        // Update status to picked up
        reservation.setStatus((byte) 4);
        reservation.setTakeDate(LocalDate.now());
        reservationRepository.save(reservation);

        return new ReservationResponseDTO("Pickup confirmed", (byte) 4);
    }

    /**
     * Get user's reservation list
     */
    @Transactional(readOnly = true)
    public List<AdminReservationDTO> getUserReservations(Integer userId) {
        List<Reservation> reservations = reservationRepository.findByUserIdOrderByCreateDateDesc(userId);

        return reservations.stream().map(res -> {
            String bookName = bookRepository.findById(res.getBookId())
                    .map(Book::getBookName)
                    .orElse("Unknown Book");

            String userNickname = userRepository.findById(res.getUserId())
                    .map(UserEntity::getNickname)
                    .orElse("Unknown User");

            return new AdminReservationDTO(
                    res.getReservationId(),
                    res.getBookId(),  // Add bookId
                    bookName,
                    userNickname,
                    res.getCreateDate(),
                    res.getTakeDate(),
                    res.getReturnDate(),
                    res.getStatus()
            );
        }).collect(Collectors.toList());
    }

}

