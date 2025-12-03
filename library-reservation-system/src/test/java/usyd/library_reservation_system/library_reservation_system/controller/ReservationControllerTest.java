package usyd.library_reservation_system.library_reservation_system.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import usyd.library_reservation_system.library_reservation_system.dto.AdminReservationDTO;
import usyd.library_reservation_system.library_reservation_system.dto.ReservationDTO;
import usyd.library_reservation_system.library_reservation_system.dto.ReservationResponseDTO;
import usyd.library_reservation_system.library_reservation_system.service.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ReservationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController reservationController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(reservationController).build();
        objectMapper = new ObjectMapper();
    }

    // region reserveBook()
    @Test
    void testReserveBook_ShouldReturnOk() throws Exception {
        ReservationDTO dto = new ReservationDTO();
        dto.setUserId(1);
        dto.setBookId(100);

        ReservationResponseDTO response = new ReservationResponseDTO("Reservation successful", (byte) 1);
        when(reservationService.reserveBook(anyInt(), anyInt())).thenReturn(response);

        mockMvc.perform(post("/api/reservations/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reservation successful"))
                .andExpect(jsonPath("$.status").value(1));

        verify(reservationService).reserveBook(1, 100);
    }
    // endregion

    // region cancelReservation()
    @Test
    void testCancelReservation_ShouldReturnSuccess() throws Exception {
        ReservationResponseDTO response = new ReservationResponseDTO("Cancellation successful", (byte) 3);
        when(reservationService.cancelReservation(anyInt(), anyInt())).thenReturn(response);

        String jsonBody = """
            {
              "userId": 1,
              "reservationId": 5
            }
        """;

        mockMvc.perform(post("/api/reservations/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cancellation successful"))
                .andExpect(jsonPath("$.status").value(3));

        verify(reservationService).cancelReservation(1, 5);
    }
    // endregion

    // region returnBook()
    @Test
    void testReturnBook_ShouldReturnSuccess() throws Exception {
        when(reservationService.returnBook(5))
                .thenReturn(new ReservationResponseDTO("Return successful", (byte) 2));

        mockMvc.perform(post("/api/reservations/return/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Return successful"))
                .andExpect(jsonPath("$.status").value(2));

        verify(reservationService).returnBook(5);
    }
    // endregion

    // region getAllReservations()
    @Test
    void testGetAllReservations_ShouldReturnList() throws Exception {
        AdminReservationDTO dto = new AdminReservationDTO(1, 100, "Book A", "User A",
                null, null, null, (byte) 1);

        when(reservationService.getAllReservations(anyByte()))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/reservations/admin/all")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookName").value("Book A"))
                .andExpect(jsonPath("$[0].userNickname").value("User A"));

        verify(reservationService).getAllReservations((byte) 1);
    }
    // endregion

    // region approveTakeBook()
    @Test
    void testApproveTakeBook_ShouldReturnOk() throws Exception {
        when(reservationService.approveTakeBook(10))
                .thenReturn(new ReservationResponseDTO("Pickup confirmed", (byte) 4));

        mockMvc.perform(post("/api/reservations/admin/approve/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pickup confirmed"))
                .andExpect(jsonPath("$.status").value(4));

        verify(reservationService).approveTakeBook(10);
    }
    // endregion

    // region getUserReservations()
    @Test
    void testGetUserReservations_ShouldReturnList() throws Exception {
        AdminReservationDTO dto = new AdminReservationDTO(1, 100, "Book A", "User A",
                null, null, null, (byte) 1);

        when(reservationService.getUserReservations(1)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/reservations")
                        .header("X-USER-ID", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookName").value("Book A"))
                .andExpect(jsonPath("$[0].userNickname").value("User A"));

        verify(reservationService).getUserReservations(1);
    }

    @Test
    void testGetUserReservations_NoHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPickupBook_ShouldReturnUpdatedReservation() throws Exception {
        ReservationResponseDTO response = new ReservationResponseDTO("Book picked up successfully", (byte) 4);
        when(reservationService.pickupBook(10)).thenReturn(response);

        mockMvc.perform(post("/api/reservations/admin/pickup/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Book picked up successfully"))
                .andExpect(jsonPath("$.status").value(4));

        verify(reservationService).pickupBook(10);
    }

    @Test
    void testAutoAssign_ShouldReturnAssignedReservation() throws Exception {
        ReservationResponseDTO response = new ReservationResponseDTO("Auto-assign success", (byte) 1);
        when(reservationService.autoAssignNextUser(99)).thenReturn(response);

        mockMvc.perform(post("/api/reservations/auto-assign/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Auto-assign success"))
                .andExpect(jsonPath("$.status").value(1));

        verify(reservationService).autoAssignNextUser(99);
    }

    @Test
    void testDeleteReservation_ShouldReturnCancelled() throws Exception {
        ReservationResponseDTO response = new ReservationResponseDTO("Reservation cancelled", (byte) 3);
        when(reservationService.cancelReservation(5, 123)).thenReturn(response);

        mockMvc.perform(delete("/api/reservations/123")
                        .header("X-USER-ID", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reservation cancelled"))
                .andExpect(jsonPath("$.status").value(3));

        verify(reservationService).cancelReservation(5, 123);
    }

    @Test
    void testDeleteReservation_ShouldReturnBadRequest_WhenUserIdMissing() throws Exception {
        mockMvc.perform(delete("/api/reservations/123"))
                .andExpect(status().isBadRequest());
    }

    // endregion
}
