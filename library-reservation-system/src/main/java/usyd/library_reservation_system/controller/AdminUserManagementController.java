package usyd.library_reservation_system.library_reservation_system.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import usyd.library_reservation_system.library_reservation_system.dto.AdminActionResponse;
import usyd.library_reservation_system.library_reservation_system.dto.AdminUserSummaryDTO;
import usyd.library_reservation_system.library_reservation_system.service.AdminUserManagementService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*") // 若前后端同源，可去掉
@RequiredArgsConstructor
public class AdminUserManagementController {

    private final AdminUserManagementService service;

    // 1) 搜索（按 username=nickname 模糊查询）
    // GET /api/admin/users/search?username=xxx
    @GetMapping("/search")
    public ResponseEntity<List<AdminUserSummaryDTO>> search(@RequestParam("username") String username) {
        return ResponseEntity.ok(service.searchByNickname(username));
    }

    // 2) 重置密码为默认值（哈希后存）
    // POST /api/admin/users/{userId}/reset-password
    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<AdminActionResponse> resetPassword(@PathVariable Integer userId) {
        String defaultPassword = "123456789"; // 你可改为从配置读取：@Value("${app.admin.default-password:123456789}")
        return ResponseEntity.ok(service.resetPasswordToDefault(userId, defaultPassword));
    }

    // 封禁
    @PatchMapping("/{id}/ban")
    public ResponseEntity<Void> ban(@PathVariable Integer id){
        service.banUser(id);
        return ResponseEntity.noContent().build();
    }

    // 解封
    @PatchMapping("/{id}/unban")
    public ResponseEntity<Void> unban(@PathVariable Integer id){
        service.unbanUser(id);
        return ResponseEntity.noContent().build();
    }
}
