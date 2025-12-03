package usyd.library_reservation_system.library_reservation_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import usyd.library_reservation_system.library_reservation_system.dto.*;
import usyd.library_reservation_system.library_reservation_system.service.AuthService_UserLogin;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController_UserLogin {

    private final AuthService_UserLogin authService; // 类型注入

    // Step1：请求验证码（返回 challengeToken）
    @PostMapping("/login/request-code")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public LoginStartResponse requestLoginCode(@Valid @RequestBody LoginStartRequest req) {
        return authService.startLogin(req);
    }

    // Step2：提交验证码 + challengeToken，校验通过即登录成功
    @PostMapping("/login/verify")
    public LoginSuccessResponse verify(@Valid @RequestBody LoginVerifyRequest req) {
        return authService.verifyLogin(req);
    }
}
