package usyd.library_reservation_system.library_reservation_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import usyd.library_reservation_system.library_reservation_system.dto.ChangePasswordReq;
import usyd.library_reservation_system.library_reservation_system.dto.UserProfileDTO;
import usyd.library_reservation_system.library_reservation_system.service.UserProfileService;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(
        origins = "*",
        methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH,
                RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS }
)
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * 获取用户资料（示例用路径参数；若已有登录态，推荐 GET /api/me）
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable Integer userId) {
        return ResponseEntity.ok(userProfileService.getProfile(userId));
    }

    /**
     * 修改密码（示例用路径参数；若有登录态，推荐 PATCH /api/me/password）
     */
    @PostMapping("/{userId}/password")
    public ResponseEntity<?> changePasswordPost(@PathVariable Integer userId,
                                                @Valid @RequestBody ChangePasswordReq req) {
        userProfileService.changePassword(userId, req);
        return ResponseEntity.ok(Map.of("message", "PASSWORD_UPDATED"));
    }
}
