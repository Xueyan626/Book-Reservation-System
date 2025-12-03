package usyd.library_reservation_system.library_reservation_system.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import usyd.library_reservation_system.library_reservation_system.dto.AdminPwdLoginReq;
import usyd.library_reservation_system.library_reservation_system.dto.adminlogin.AdminLoginResp;
import usyd.library_reservation_system.library_reservation_system.service.AuthService_AdminLogin;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AuthController_AdminLogin {

    private final AuthService_AdminLogin service;

    // 单步：邮箱 + 明文密码
    @PostMapping("/login")
    public ResponseEntity<AdminLoginResp> login(@Valid @RequestBody AdminPwdLoginReq req) {
        return ResponseEntity.ok(service.pwdLogin(req));
    }
}
