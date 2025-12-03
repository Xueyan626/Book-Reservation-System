package usyd.library_reservation_system.library_reservation_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usyd.library_reservation_system.library_reservation_system.dto.*;
import usyd.library_reservation_system.library_reservation_system.model.UserEntity;
import usyd.library_reservation_system.library_reservation_system.repository.UserRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService_UserRegister {

    private final UserRepository userRepository;
    private final EmailService emailService;       // Available: sendRegisterCode with 3 params
    private final TokenService tokenService;       // Available: sign(Map, Instant)/verify(String)
    private final PasswordEncoder passwordEncoder; // From SecurityBeans

    @Value("${app.login.code-ttl-minutes:10}")
    private int ttlMinutes;                        // Plan B: passed by caller or injected here

    @Value("${app.login.code-length:6}")
    private int codeLength;

    // Step 1: Send verification code (without saving to database)
    public RegisterStartResponse startRegister(RegisterStartRequest req) {
        final String email = req.getEmail().trim().toLowerCase();

        // Generate numeric verification code
        String code = randomDigits(codeLength);

        // Use TokenService (JJWT) to generate challengeToken (containing email + code) and set expiration
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("code", code);
        Instant expiresAt = Instant.now().plusSeconds(ttlMinutes * 60L);
        String challengeToken = tokenService.sign(claims, expiresAt);

        // Send email (Plan B: explicitly pass ttlMinutes)
        emailService.sendRegisterCode(email, code, ttlMinutes);

        return RegisterStartResponse.builder()
                .challengeToken(challengeToken)
                .ttlMinutes(ttlMinutes)
                .build();
    }

    // Step 2: Submit registration (verify token + code + password match, hash storage)
    public UserProfileDTO submitRegister(RegisterSubmitRequest req) {
        // 1) Verify challenge (internally verifies signature & expiration)
        Map<String, Object> payload;
        try {
            payload = tokenService.verify(req.getChallengeToken());
        }catch (IllegalArgumentException e) {           // This causes the 500 error
            // Log the actual reason, e.g., "expired" / "bad signature" / "bad token"
            throw new InvalidCredentialsException(e.getMessage());  // Let global exception handler return 400
        }

        String tokenEmail = String.valueOf(payload.get("email"));
        String tokenCode  = String.valueOf(payload.get("code"));

        // 2) Prevent email tampering
        if (!tokenEmail.equalsIgnoreCase(req.getEmail().trim())) {
            throw new InvalidCredentialsException("email", "email mismatch");
        }

        // 3) Verify code matches
        if (!tokenCode.equals(req.getCode().trim())) {
            throw new InvalidCredentialsException("code mismatch");
        }

        // 4) Password confirmation matches
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new InvalidCredentialsException("password not match");
        }

        // 5) Generate hash (BCrypt)
        String hash = passwordEncoder.encode(req.getPassword());

        // 6) Save to database
        UserEntity u = new UserEntity();
        u.setNickname(req.getNickname());
        u.setEmail(req.getEmail().trim().toLowerCase());
        u.setTelephone(req.getTelephone());      // Using telephone field
        u.setPasswordHash(hash);                 // Using passwordHash field
        u.setIsActive(Boolean.TRUE);
        userRepository.save(u);

        // 7) Return simplified profile to frontend (according to your DTO fields)
        UserProfileDTO dto = new UserProfileDTO();
        dto.setUserId(u.getUserId());
        dto.setNickname(u.getNickname());
        dto.setEmail(u.getEmail());
        dto.setTelephone(u.getTelephone());
        return dto;
    }


    private String randomDigits(int len) {
        var r = ThreadLocalRandom.current();
        var sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(r.nextInt(10));
        return sb.toString();
    }
}
