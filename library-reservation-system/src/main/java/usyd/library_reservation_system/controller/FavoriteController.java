package usyd.library_reservation_system.library_reservation_system.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import usyd.library_reservation_system.library_reservation_system.service.FavoriteService;
import usyd.library_reservation_system.library_reservation_system.dto.BookCardDto;
import java.util.List;
import java.util.Map;
import usyd.library_reservation_system.library_reservation_system.service.FavoriteService;
import usyd.library_reservation_system.library_reservation_system.repository.BookRepository;
import usyd.library_reservation_system.library_reservation_system.model.Book;
import usyd.library_reservation_system.library_reservation_system.model.favorite.FavoriteId;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/toggle")
    public ResponseEntity<?> toggle(@RequestBody ToggleReq req,
                                    jakarta.servlet.http.HttpServletRequest httpReq) {
        // 统一从 Header 取用户
        String h = httpReq.getHeader("X-USER-ID");
        if (h == null || !h.matches("\\d+")) {
            return ResponseEntity.status(401)
                    .body(java.util.Map.of("error","UNAUTHORIZED","message","Missing X-USER-ID header"));
        }
        Integer userId = Integer.valueOf(h);

        if (req.getBookId() == null) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error","BAD_REQUEST","message","bookId is required"));
        }
        var result = favoriteService.toggle(userId, req.getBookId());
        return ResponseEntity.ok(result);
    }

    @Data
    public static class ToggleReq {
        private Integer userId;
        private Integer bookId;
    }

    private Integer currentUserIdFromHeader(jakarta.servlet.http.HttpServletRequest req) {
//        String h = req.getHeader("X-USER-ID");
//        if (h != null && h.matches("\\d+")) return Integer.valueOf(h);
//        throw new RuntimeException("Missing X-USER-ID header");
        String h = req.getHeader("X-USER-ID");
        return (h != null && h.matches("\\d+")) ? Integer.valueOf(h) : null;
    }

    @GetMapping("/{bookId}/check")
    public Map<String, Object> check(@PathVariable Integer bookId,
                                     jakarta.servlet.http.HttpServletRequest req) {
        Integer userId = currentUserIdFromHeader(req);
        boolean favorited = favoriteService.exists(userId, bookId);   // 见 4.2 新增的 exists()
        return Map.of("favorited", favorited);
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<?> remove(@PathVariable Integer bookId,
                                    jakarta.servlet.http.HttpServletRequest req) {
        Integer userId = currentUserIdFromHeader(req);
        favoriteService.remove(userId, bookId);                       // 见 4.2 新增的 remove()
        return ResponseEntity.noContent().build();
    }

    @GetMapping("")
    public org.springframework.http.ResponseEntity<?> myFavorites(jakarta.servlet.http.HttpServletRequest req) {
        try {
            Integer userId = currentUserIdFromHeader(req);
            log.info("GET /api/favorites called, userId={}", userId);

            // 用 Service 拿书列表
            java.util.List<usyd.library_reservation_system.library_reservation_system.model.Book> books =
                    favoriteService.listBooksByUser(userId);

            // ✅ 只手动挑字段，避免实体序列化问题
            var result = books.stream().map(b -> java.util.Map.of(
                    "bookId",   b.getBookId(),
                    "bookName", b.getBookName(),
                    "author",   b.getAuthor(),
                    "avatar",   b.getAvatar(),
                    "labelId",  b.getLabelId()
            )).toList();

            log.info("GET /api/favorites ok, size={}", result.size());
            return org.springframework.http.ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("GET /api/favorites failed", e);   // ✅ 一定打印堆栈
            return org.springframework.http.ResponseEntity.status(500).body(java.util.Map.of(
                    "error", "INTERNAL_ERROR",
                    "message", e.getMessage() == null ? "Unexpected error" : e.getMessage()
            ));
        }
    }

    // 可选：批量删除
    @DeleteMapping("/batch")
    public ResponseEntity<?> removeBatch(@RequestBody Map<String, List<Integer>> body,
                                         jakarta.servlet.http.HttpServletRequest req) {
        Integer userId = currentUserIdFromHeader(req);
        List<Integer> ids = body.getOrDefault("bookIds", List.of());
        ids.forEach(bookId -> favoriteService.remove(userId, bookId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<?> incrementFavoriteCount(@PathVariable Integer id,
                                                    jakarta.servlet.http.HttpServletRequest req) {
        String h = req.getHeader("X-USER-ID");
        if (h == null || !h.matches("\\d+")) {
            return ResponseEntity.status(401)
                    .body(java.util.Map.of("error","UNAUTHORIZED","message","Missing X-USER-ID header"));
        }
        Integer userId = Integer.valueOf(h);
        try {
            favoriteService.add(userId, id);
            return ResponseEntity.ok().build();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "error","BAD_REQUEST","message","Invalid userId or bookId (FK/constraint failed)"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of(
                    "error","INTERNAL_ERROR","message", e.getMessage()
            ));
        }
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGeneric(Exception ex) {
        // 建议同时在日志里打印
        log.error("Unhandled exception", ex);
        return Map.of("error", "INTERNAL_ERROR", "message", ex.getMessage() == null ? "Unexpected error" : ex.getMessage());
    }



}
