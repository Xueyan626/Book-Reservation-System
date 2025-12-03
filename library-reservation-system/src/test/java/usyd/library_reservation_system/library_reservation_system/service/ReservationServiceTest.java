package usyd.library_reservation_system.library_reservation_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import usyd.library_reservation_system.library_reservation_system.dto.AdminReservationDTO;
import usyd.library_reservation_system.library_reservation_system.dto.ReservationResponseDTO;
import usyd.library_reservation_system.library_reservation_system.model.Book;
import usyd.library_reservation_system.library_reservation_system.model.Reservation;
import usyd.library_reservation_system.library_reservation_system.model.UserEntity;
import usyd.library_reservation_system.library_reservation_system.repository.BookRepository;
import usyd.library_reservation_system.library_reservation_system.repository.ReservationRepository;
import usyd.library_reservation_system.library_reservation_system.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReservationServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationService reservationService;

    private UserEntity mockUser;
    private Book mockBook;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockUser = new UserEntity();
        mockUser.setUserId(1);
        mockUser.setNickname("Alice");

        mockBook = new Book();
        mockBook.setBookId(100);
        mockBook.setBookName("Test Book");
        mockBook.setQuantity(2);
        mockBook.setNumReservation(0);
    }

    @Test
    void testReserveBook_WithStock_ShouldAssign() {
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(bookRepository.findById(100)).thenReturn(Optional.of(mockBook));

        Reservation savedRes = new Reservation();
        savedRes.setReservationId(1);
        savedRes.setStatus((byte) 1);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedRes);
        when(bookRepository.save(any(Book.class))).thenReturn(mockBook);

        ReservationResponseDTO result = reservationService.reserveBook(1, 100);

        assertEquals("Reservation successful", result.getMessage());
//        assertEquals(1, result.getStatus());
        verify(bookRepository, times(1)).save(any(Book.class)); // Save and update stock
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void testReserveBook_NoStock_ShouldQueue() {
        mockBook.setQuantity(0);
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(bookRepository.findById(100)).thenReturn(Optional.of(mockBook));

        ReservationResponseDTO result = reservationService.reserveBook(1, 100);
        assertEquals((byte) 0, result.getStatus());
    }

    @Test
    void testPickupBook_InvalidStatus_ShouldFail() {
        Reservation res = new Reservation();
        res.setReservationId(1);
        res.setStatus((byte) 0); // not available
        when(reservationRepository.findById(1)).thenReturn(Optional.of(res));

        ReservationResponseDTO result = reservationService.pickupBook(1);
        assertTrue(result.getMessage().contains("not in available for pickup"));
    }

    @Test
    void testCancelReservation_Assigned_ShouldReturnStockAndAutoAssign() {
        Reservation res = new Reservation();
        res.setReservationId(1);
        res.setUserId(1);
        res.setBookId(100);
        res.setStatus((byte) 1); // assigned

        when(reservationRepository.findById(1)).thenReturn(Optional.of(res));
        when(bookRepository.findById(100)).thenReturn(Optional.of(mockBook));
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));

        ReservationResponseDTO result = reservationService.cancelReservation(1, 1);
        assertTrue(result.getMessage().contains("Cancellation successful"));
        verify(bookRepository, atLeastOnce()).save(any(Book.class));
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void testAutoAssignNextUser_NoQueue_ShouldFail() {
        when(bookRepository.findById(100)).thenReturn(Optional.of(mockBook));
        when(reservationRepository.findByBookIdAndStatusOrderByCreateDateAsc(100, (byte) 0))
                .thenReturn(Collections.emptyList());

        ReservationResponseDTO result = reservationService.autoAssignNextUser(100);
        assertTrue(result.getMessage().contains("No users in queue"));
    }

    @Test
    void testReserveBook_UserNotFound_ShouldFail() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        ReservationResponseDTO result = reservationService.reserveBook(1, 100);

        assertEquals("User not found", result.getMessage());
        assertEquals((byte) -1, result.getStatus());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testReserveBook_BookNotFound_ShouldFail() {
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(bookRepository.findById(100)).thenReturn(Optional.empty());

        ReservationResponseDTO result = reservationService.reserveBook(1, 100);

        assertEquals("Book not found", result.getMessage());
        assertEquals((byte) -1, result.getStatus());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testCancelReservation_NotUser_ShouldFail() {
        Reservation reservation = new Reservation();
        reservation.setReservationId(1);
        reservation.setUserId(2); // Different user
        reservation.setBookId(100);
        reservation.setStatus((byte) 1);

        when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));

        ReservationResponseDTO result = reservationService.cancelReservation(1, 1);

        assertEquals("No permission to cancel others' reservation", result.getMessage());
        assertEquals((byte) -1, result.getStatus());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testReturnBook_Valid_ShouldUpdateStatusAndTriggerAutoAssign() {
        // Simulate borrowed reservation
        Reservation res = new Reservation();
        res.setReservationId(1);
        res.setStatus((byte) 4);
        res.setBookId(100);

        when(reservationRepository.findById(1)).thenReturn(Optional.of(res));
        when(bookRepository.findById(100)).thenReturn(Optional.of(mockBook));

        // Simulate autoAssignNextUser result
        when(reservationRepository.findByBookIdAndStatusOrderByCreateDateAsc(100, (byte) 0))
                .thenReturn(Collections.emptyList());

        ReservationResponseDTO result = reservationService.returnBook(1);

        assertTrue(result.getMessage().contains("Return successful"));
        assertEquals((byte) 2, result.getStatus());
        verify(bookRepository, atLeastOnce()).save(any(Book.class));
        verify(reservationRepository, atLeastOnce()).save(any(Reservation.class));
    }

    @Test
    void testApproveTakeBook_ValidAndInvalid() {
        // Case 1: Normal approval
        Reservation res1 = new Reservation();
        res1.setReservationId(1);
        res1.setStatus((byte) 1);
        when(reservationRepository.findById(1)).thenReturn(Optional.of(res1));

        ReservationResponseDTO success = reservationService.approveTakeBook(1);
        assertEquals("Pickup confirmed", success.getMessage());
        assertEquals((byte) 4, success.getStatus());
        verify(reservationRepository, atLeastOnce()).save(any());

        // Case 2: Status error
        Reservation res2 = new Reservation();
        res2.setReservationId(2);
        res2.setStatus((byte) 0);
        when(reservationRepository.findById(2)).thenReturn(Optional.of(res2));

        ReservationResponseDTO fail = reservationService.approveTakeBook(2);
        assertTrue(fail.getMessage().contains("not in available for pickup state"));
        assertEquals((byte) 0, fail.getStatus());

        // Case 3: Not exists
        when(reservationRepository.findById(3)).thenReturn(Optional.empty());
        ReservationResponseDTO notFound = reservationService.approveTakeBook(3);
        assertEquals("Reservation not found", notFound.getMessage());
    }

    @Test
    void testCancelReservation_ReservationNotFound_ShouldFail() {
        when(reservationRepository.findById(1)).thenReturn(Optional.empty());

        ReservationResponseDTO result = reservationService.cancelReservation(1, 1);

        assertEquals("Reservation not found", result.getMessage());
        assertEquals((byte) -1, result.getStatus());
    }

    @Test
    void testCancelReservation_AlreadyCancelled_ShouldFail() {
        Reservation res = new Reservation();
        res.setReservationId(1);
        res.setUserId(1);
        res.setStatus((byte) 3); // Already cancelled

        when(reservationRepository.findById(1)).thenReturn(Optional.of(res));

        ReservationResponseDTO result = reservationService.cancelReservation(1, 1);

        assertEquals("Reservation is already completed or cancelled", result.getMessage());
        assertEquals((byte) 3, result.getStatus());
    }

    @Test
    void testReturnBook_ReservationNotFound_ShouldFail() {
        when(reservationRepository.findById(1)).thenReturn(Optional.empty());

        ReservationResponseDTO result = reservationService.returnBook(1);

        assertEquals("Reservation not found", result.getMessage());
        assertEquals((byte) -1, result.getStatus());
    }

    @Test
    void testReturnBook_InvalidStatus_ShouldFail() {
        Reservation res = new Reservation();
        res.setReservationId(1);
        res.setBookId(100);
        res.setStatus((byte) 1);

        when(reservationRepository.findById(1)).thenReturn(Optional.of(res));

        ReservationResponseDTO result = reservationService.returnBook(1);

        assertTrue(result.getMessage().contains("not in returnable state"));
        assertEquals((byte) 1, result.getStatus());
    }

    @Test
    void testAutoAssignNextUser_BookNotFound_ShouldFail() {
        when(bookRepository.findById(100)).thenReturn(Optional.empty());

        ReservationResponseDTO result = reservationService.autoAssignNextUser(100);

        assertEquals("Book not found", result.getMessage());
        assertEquals((byte) -1, result.getStatus());
    }

    @Test
    void testAutoAssignNextUser_InsufficientStock_ShouldFail() {
        mockBook.setQuantity(0);
        when(bookRepository.findById(100)).thenReturn(Optional.of(mockBook));

        ReservationResponseDTO result = reservationService.autoAssignNextUser(100);

        assertEquals("Insufficient stock, unable to assign", result.getMessage());
        assertEquals((byte) -1, result.getStatus());
    }
    @Test
    void testAutoAssignNextUser_EmptyQueue_ShouldFail() {
        mockBook.setQuantity(5);
        when(bookRepository.findById(100)).thenReturn(Optional.of(mockBook));
        when(reservationRepository.findByBookIdAndStatusOrderByCreateDateAsc(100, (byte) 0))
                .thenReturn(List.of());

        ReservationResponseDTO result = reservationService.autoAssignNextUser(100);

        assertEquals("No users in queue", result.getMessage());
        assertEquals((byte) -1, result.getStatus());
    }

    @Test
    void testAutoAssignNextUser_WithQueue_ShouldAssignFirstUser() {
        mockBook.setQuantity(3);
        Reservation waitingRes = new Reservation();
        waitingRes.setReservationId(10);
        waitingRes.setBookId(100);
        waitingRes.setUserId(7);
        waitingRes.setStatus((byte) 0);

        when(bookRepository.findById(100)).thenReturn(Optional.of(mockBook));
        when(reservationRepository.findByBookIdAndStatusOrderByCreateDateAsc(100, (byte) 0))
                .thenReturn(List.of(waitingRes));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(waitingRes);

        ReservationResponseDTO result = reservationService.autoAssignNextUser(100);

        assertTrue(result.getMessage().contains("Assigned to user ID: 7"));
        assertEquals((byte) 1, result.getStatus());
        verify(bookRepository).save(any(Book.class));
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void testGetAllReservations_StatusNull_ShouldReturnAll() {
        Reservation res = new Reservation();
        res.setReservationId(1);
        res.setUserId(1);
        res.setBookId(100);
        res.setStatus((byte) 1);

        when(reservationRepository.findAll()).thenReturn(List.of(res));
        when(bookRepository.findById(100)).thenReturn(Optional.of(mockBook));
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));

        List<AdminReservationDTO> list = reservationService.getAllReservations(null);

        assertEquals(1, list.size());
        assertEquals("Test Book", list.get(0).getBookName());
        assertEquals("Alice", list.get(0).getUserNickname());
    }

    @Test
    void testGetAllReservations_WithSpecificStatus_ShouldReturnFiltered() {
        Reservation res1 = new Reservation();
        res1.setReservationId(1);
        res1.setUserId(1);
        res1.setBookId(100);
        res1.setStatus((byte) 1);

        Reservation res2 = new Reservation();
        res2.setReservationId(2);
        res2.setUserId(1);
        res2.setBookId(100);
        res2.setStatus((byte) 0);

        when(reservationRepository.findByStatus((byte) 1)).thenReturn(List.of(res1));
        when(bookRepository.findById(100)).thenReturn(Optional.of(mockBook));
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));

        List<AdminReservationDTO> list = reservationService.getAllReservations((byte) 1);

        assertEquals(1, list.size());
        assertEquals((byte) 1, list.get(0).getStatus());
    }

    @Test
    void testPickupBook_ReservationNotFound_ShouldFail() {
        when(reservationRepository.findById(999)).thenReturn(Optional.empty());

        ReservationResponseDTO result = reservationService.pickupBook(999);

        assertEquals("Reservation not found", result.getMessage());
        assertEquals((byte) -1, result.getStatus());
    }

    @Test
    void testPickupBook_ValidStatus_ShouldSuccess() {
        Reservation res = new Reservation();
        res.setReservationId(1);
        res.setStatus((byte) 1);

        when(reservationRepository.findById(1)).thenReturn(Optional.of(res));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(res);

        ReservationResponseDTO result = reservationService.pickupBook(1);

        assertEquals("User has picked up the book, status updated to picked up", result.getMessage());
        assertEquals((byte) 4, result.getStatus());
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void testCancelReservation_QueueingStatus_ShouldCancelWithoutStockReturn() {
        Reservation res = new Reservation();
        res.setReservationId(1);
        res.setUserId(1);
        res.setBookId(100);
        res.setStatus((byte) 0); // Queuing

        when(reservationRepository.findById(1)).thenReturn(Optional.of(res));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(res);

        ReservationResponseDTO result = reservationService.cancelReservation(1, 1);

        assertEquals("Cancellation successful", result.getMessage());
        assertEquals((byte) 3, result.getStatus());
        verify(bookRepository, never()).save(any(Book.class)); // No stock return for queuing
    }

    @Test
    void testCancelReservation_BorrowedStatus_ShouldNotCancel() {
        Reservation res = new Reservation();
        res.setReservationId(1);
        res.setUserId(1);
        res.setBookId(100);
        res.setStatus((byte) 4); // Borrowed

        when(reservationRepository.findById(1)).thenReturn(Optional.of(res));

        ReservationResponseDTO result = reservationService.cancelReservation(1, 1);

        // Status 4 is not 2 or 3, so it can be cancelled
        assertEquals("Cancellation successful", result.getMessage());
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void testCancelReservation_WithReturnedStatus_ShouldFail() {
        Reservation res = new Reservation();
        res.setReservationId(1);
        res.setUserId(1);
        res.setStatus((byte) 2); // Returned

        when(reservationRepository.findById(1)).thenReturn(Optional.of(res));

        ReservationResponseDTO result = reservationService.cancelReservation(1, 1);

        assertEquals("Reservation is already completed or cancelled", result.getMessage());
        assertEquals((byte) 2, result.getStatus());
    }

    @Test
    void testReturnBook_BookNotFound_ShouldStillReturnSuccess() {
        Reservation res = new Reservation();
        res.setReservationId(1);
        res.setBookId(100);
        res.setStatus((byte) 4);

        when(reservationRepository.findById(1)).thenReturn(Optional.of(res));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(res);
        when(bookRepository.findById(100)).thenReturn(Optional.empty());

        ReservationResponseDTO result = reservationService.returnBook(1);

        assertEquals("Return successful", result.getMessage());
        assertEquals((byte) 2, result.getStatus());
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void testReturnBook_AutoAssignFails_ShouldStillReturnSuccess() {
        Reservation res = new Reservation();
        res.setReservationId(1);
        res.setBookId(100);
        res.setStatus((byte) 4);

        when(reservationRepository.findById(1)).thenReturn(Optional.of(res));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(res);
        when(bookRepository.findById(100)).thenReturn(Optional.of(mockBook));
        when(bookRepository.save(any(Book.class))).thenReturn(mockBook);
        when(reservationRepository.findByBookIdAndStatusOrderByCreateDateAsc(100, (byte) 0))
                .thenReturn(List.of()); // No queue

        ReservationResponseDTO result = reservationService.returnBook(1);

        assertEquals("Return successful", result.getMessage());
        assertEquals((byte) 2, result.getStatus());
    }

    @Test
    void testReturnBook_WithSuccessfulAutoAssign_ShouldReturnWithAssignMessage() {
        Reservation res = new Reservation();
        res.setReservationId(1);
        res.setBookId(100);
        res.setStatus((byte) 4);

        Reservation waitingRes = new Reservation();
        waitingRes.setReservationId(2);
        waitingRes.setBookId(100);
        waitingRes.setUserId(2);
        waitingRes.setStatus((byte) 0);

        when(reservationRepository.findById(1)).thenReturn(Optional.of(res));
        when(bookRepository.findById(100)).thenReturn(Optional.of(mockBook));
        when(reservationRepository.findByBookIdAndStatusOrderByCreateDateAsc(100, (byte) 0))
                .thenReturn(List.of(waitingRes));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookRepository.save(any(Book.class))).thenReturn(mockBook);

        ReservationResponseDTO result = reservationService.returnBook(1);

        assertTrue(result.getMessage().contains("Return successful, automatically assigned to next user"));
        assertEquals((byte) 2, result.getStatus());
    }

    @Test
    void testCancelReservation_AssignedWithAutoAssignSuccess_ShouldReturnWithMessage() {
        Reservation res = new Reservation();
        res.setReservationId(1);
        res.setUserId(1);
        res.setBookId(100);
        res.setStatus((byte) 1); // Assigned

        Reservation waitingRes = new Reservation();
        waitingRes.setReservationId(2);
        waitingRes.setBookId(100);
        waitingRes.setUserId(2);
        waitingRes.setStatus((byte) 0);

        when(reservationRepository.findById(1)).thenReturn(Optional.of(res));
        when(bookRepository.findById(100)).thenReturn(Optional.of(mockBook));
        when(reservationRepository.findByBookIdAndStatusOrderByCreateDateAsc(100, (byte) 0))
                .thenReturn(List.of(waitingRes));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookRepository.save(any(Book.class))).thenReturn(mockBook);

        ReservationResponseDTO result = reservationService.cancelReservation(1, 1);

        assertTrue(result.getMessage().contains("Return successful, automatically assigned to next user"));
        assertEquals((byte) 2, result.getStatus());
    }

    @Test
    void testGetUserReservations_ShouldReturnUserSpecificReservations() {
        Reservation res1 = new Reservation();
        res1.setReservationId(1);
        res1.setUserId(1);
        res1.setBookId(100);
        res1.setStatus((byte) 1);
        res1.setCreateDate(LocalDateTime.now());

        Reservation res2 = new Reservation();
        res2.setReservationId(2);
        res2.setUserId(1);
        res2.setBookId(100);
        res2.setStatus((byte) 0);
        res2.setCreateDate(LocalDateTime.now().minusDays(1));

        when(reservationRepository.findByUserIdOrderByCreateDateDesc(1)).thenReturn(List.of(res1, res2));
        when(bookRepository.findById(100)).thenReturn(Optional.of(mockBook));
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));

        List<AdminReservationDTO> result = reservationService.getUserReservations(1);

        assertEquals(2, result.size());
        assertEquals("Test Book", result.get(0).getBookName());
        assertEquals("Alice", result.get(0).getUserNickname());
    }

    @Test
    void testGetUserReservations_WithUnknownBookAndUser_ShouldReturnUnknown() {
        Reservation res = new Reservation();
        res.setReservationId(1);
        res.setUserId(999);
        res.setBookId(999);
        res.setStatus((byte) 1);
        res.setCreateDate(LocalDateTime.now());

        when(reservationRepository.findByUserIdOrderByCreateDateDesc(999)).thenReturn(List.of(res));
        when(bookRepository.findById(999)).thenReturn(Optional.empty());
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        List<AdminReservationDTO> result = reservationService.getUserReservations(999);

        assertEquals(1, result.size());
        assertEquals("Unknown Book", result.get(0).getBookName());
        assertEquals("Unknown User", result.get(0).getUserNickname());
    }

    @Test
    void testGetAllReservations_WithUnknownBookAndUser_ShouldReturnUnknown() {
        Reservation res = new Reservation();
        res.setReservationId(1);
        res.setUserId(999);
        res.setBookId(999);
        res.setStatus((byte) 1);

        when(reservationRepository.findAll()).thenReturn(List.of(res));
        when(bookRepository.findById(999)).thenReturn(Optional.empty());
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        List<AdminReservationDTO> result = reservationService.getAllReservations(null);

        assertEquals(1, result.size());
        assertEquals("Unknown Book", result.get(0).getBookName());
        assertEquals("Unknown User", result.get(0).getUserNickname());
    }



}

