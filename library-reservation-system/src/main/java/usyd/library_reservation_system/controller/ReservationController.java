package usyd.library_reservation_system.library_reservation_system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import usyd.library_reservation_system.library_reservation_system.dto.AdminReservationDTO;
import usyd.library_reservation_system.library_reservation_system.dto.CancelReservationDTO;
import usyd.library_reservation_system.library_reservation_system.dto.ReservationDTO;
import usyd.library_reservation_system.library_reservation_system.dto.ReservationResponseDTO;
import usyd.library_reservation_system.library_reservation_system.service.ReservationService;

import java.util.List;

// Reservation.status 取值：
// 0 = 排队中
// 1 = 已分配/可取书
// 2 = 已归还
// 3 = 已取消
// 4 = 已取书
@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*",
        allowedHeaders = {"*", "X-USER-ID"},
        exposedHeaders = {"X-USER-ID"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * 用户预定书籍
     */
    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponseDTO> reserveBook(@RequestBody ReservationDTO dto) {
        ReservationResponseDTO response = reservationService.reserveBook(dto.getUserId(), dto.getBookId());
        return ResponseEntity.ok(response);
    }

    /**
     * 管理员确认用户取书（从状态1改为4）
     */
    @PostMapping("/admin/pickup/{reservationId}")
    public ResponseEntity<ReservationResponseDTO> pickupBook(@PathVariable Integer reservationId) {
        ReservationResponseDTO response = reservationService.pickupBook(reservationId);
        return ResponseEntity.ok(response);
    }

    /**
     * 用户取消预定
     */
    @PostMapping("/cancel")
    public ResponseEntity<ReservationResponseDTO> cancelReservation(@RequestBody CancelReservationDTO dto) {
        ReservationResponseDTO response = reservationService.cancelReservation(dto.getUserId(), dto.getReservationId());
        return ResponseEntity.ok(response);
    }

    /**
     * 管理员手动触发自动分配
     */
    @PostMapping("/auto-assign/{bookId}")
    public ResponseEntity<ReservationResponseDTO> autoAssign(@PathVariable Integer bookId) {
        ReservationResponseDTO response = reservationService.autoAssignNextUser(bookId);
        return ResponseEntity.ok(response);
    }

    /**
     * 用户还书
     */
    @PostMapping("/return/{reservationId}")
    public ResponseEntity<ReservationResponseDTO> returnBook(@PathVariable Integer reservationId) {
        ReservationResponseDTO response = reservationService.returnBook(reservationId);
        return ResponseEntity.ok(response);
    }

    /**
     * 管理员查看所有预定订单详情
     */
    @GetMapping("/admin/all")
    public ResponseEntity<List<AdminReservationDTO>> getAllReservations(@RequestParam(required = false) Byte status) {
        List<AdminReservationDTO> response = reservationService.getAllReservations(status);
        return ResponseEntity.ok(response);
    }

    /**
     * 管理员同意用户取书
     */
    @PostMapping("/admin/approve/{reservationId}")
    public ResponseEntity<ReservationResponseDTO> approveTakeBook(@PathVariable Integer reservationId) {
        ReservationResponseDTO response = reservationService.approveTakeBook(reservationId);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户的订阅列表
     */
    @GetMapping
    public ResponseEntity<List<AdminReservationDTO>> getUserReservations(@RequestHeader(value = "X-USER-ID", required = false) Integer userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        List<AdminReservationDTO> reservations = reservationService.getUserReservations(userId);
        return ResponseEntity.ok(reservations);
    }

    /**
     * 删除/取消订阅
     */
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ReservationResponseDTO> deleteReservation(@PathVariable Integer reservationId, @RequestHeader(value = "X-USER-ID", required = false) Integer userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        ReservationResponseDTO response = reservationService.cancelReservation(userId, reservationId);
        return ResponseEntity.ok(response);
    }

}

