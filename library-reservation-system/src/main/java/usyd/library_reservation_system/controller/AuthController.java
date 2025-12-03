package usyd.library_reservation_system.library_reservation_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import usyd.library_reservation_system.library_reservation_system.dto.*;
import usyd.library_reservation_system.library_reservation_system.service.AuthService;
import usyd.library_reservation_system.library_reservation_system.service.AuthService_UserRegister;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final AuthService_UserRegister userRegisterService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    //new register: use varification
    // 1) 开始注册：发送验证码并返回 challengeToken
    @PostMapping("/register/start")
    public RegisterStartResponse startRegister(@RequestBody RegisterStartRequest req) {
        return userRegisterService.startRegister(req);
    }

    // 2) 提交注册：校验+落库
    @PostMapping("/register/submit")
    public UserProfileDTO submitRegister(@RequestBody RegisterSubmitRequest req) {
        return userRegisterService.submitRegister(req);
    }
}
