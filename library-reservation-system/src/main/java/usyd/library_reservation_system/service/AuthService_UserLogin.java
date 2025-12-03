package usyd.library_reservation_system.library_reservation_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import usyd.library_reservation_system.library_reservation_system.dto.LoginStartRequest;
import usyd.library_reservation_system.library_reservation_system.dto.LoginStartResponse;
import usyd.library_reservation_system.library_reservation_system.dto.LoginSuccessResponse;
import usyd.library_reservation_system.library_reservation_system.dto.LoginVerifyRequest;
import usyd.library_reservation_system.library_reservation_system.repository.UserRepository;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class AuthService_UserLogin {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TokenService tokenService;

    @Value("${app.login.code-ttl-minutes:10}")
    private int codeTtlMinutes;

    @Value("${app.login.code-length:6}")
    private int codeLength;


    public LoginStartResponse startLogin(LoginStartRequest req) {
        final String email = req.email().trim().toLowerCase();

        // To avoid user enumeration, do not reveal whether the email exists.
        var userOpt = userRepo.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            var jwt = tokenService.sign(
                    Map.of(
                            "email", email,
                            // When account doesn't exist, don't include userId; set codeHash to NA, subsequent verify will fail
                            "codeHash", "NA"
                    ),
                    Instant.now().plus(codeTtlMinutes, ChronoUnit.MINUTES)
            );
            return new LoginStartResponse("login code sent", jwt);
        }

        var user = userOpt.get();

        // Generate numeric verification code
        String code = generateNumericCode(codeLength);

        // Send email (during debugging, you may log it instead of sending real email to troubleshoot SMTP config)
        emailService.sendLoginCode(user.getEmail(), code, codeTtlMinutes);

        // Store only code hash in token to avoid plaintext storage
        String codeHash = sha256Hex(code);
        var exp = Instant.now().plus(codeTtlMinutes, ChronoUnit.MINUTES);

        var jwt = tokenService.sign(
                Map.of(
                        "userId", user.getUserId(),
                        "email", user.getEmail(),
                        "codeHash", codeHash
                ),
                exp
        );

        return new LoginStartResponse("login code sent", jwt);
    }

    /**
     * Step 2: Complete login by verifying challengeToken + code + email + password
     */
    public LoginSuccessResponse verifyLogin(LoginVerifyRequest req) {
        final Map<String,Object> claims;
        try {
            claims = tokenService.verify(req.challengeToken());
        } catch (io.jsonwebtoken.JwtException e) {
            // Token expired/signature error/format error
            throw new InvalidCredentialsException("token", "Invalid or expired token");
        }

        // 1) email must match token
        var emailInToken = ((String) claims.get("email")).toLowerCase();
        var emailInput = req.email().trim().toLowerCase();
        if (!emailInToken.equals(emailInput)) {
            throw new InvalidCredentialsException("email", "Email does not match the token");
        }

        // 2) verify code by comparing hashes
        var codeHashInToken = (String) claims.get("codeHash");
        String inputHash = sha256Hex(req.code().trim());
        if (!inputHash.equals(codeHashInToken)) {
            throw new InvalidCredentialsException("code", "Verification code is incorrect");
        }

        // 3) load user & verify password
        var user = userRepo.findByEmailIgnoreCase(emailInput)
                .orElseThrow(() -> new InvalidCredentialsException("user", "User not found"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new InvalidCredentialsException("user", "Account is inactive");
        }

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("password", "Password is incorrect");
        }

        // 4) success
        return new LoginSuccessResponse(
                "successfully login!",
                user.getUserId(),
                user.getNickname(),
                user.getEmail()
        );
    }


    // ======== helpers ========

    private String generateNumericCode(int len) {
        var rnd = new SecureRandom();
        var sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(rnd.nextInt(10));
        return sb.toString();
    }

    private String sha256Hex(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(s.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
