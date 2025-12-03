package usyd.library_reservation_system.library_reservation_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usyd.library_reservation_system.library_reservation_system.dto.RegisterRequest;
import usyd.library_reservation_system.library_reservation_system.dto.RegisterResponse;
import usyd.library_reservation_system.library_reservation_system.model.UserEntity;
import usyd.library_reservation_system.library_reservation_system.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponse register(RegisterRequest req) {
        final String email = req.email().trim().toLowerCase();
        final String tel = req.telephone().trim();

        // 1) Check duplicates in advance (DB has unique index, fallback below)
        if (userRepo.existsByEmailIgnoreCase(email)) {
            throw new EmailOrPhoneAlreadyUsedException("Email already used: " + email);
        }
        if (userRepo.existsByTelephone(tel)) {
            throw new EmailOrPhoneAlreadyUsedException("Telephone already used: " + tel);
        }

        // 2) Hash and save
        var entity = UserEntity.builder()
                .nickname(req.nickname().trim())
                .telephone(tel)
                .email(email)
                .passwordHash(passwordEncoder.encode(req.password()))
                .build();

        try {
            var saved = userRepo.save(entity);
            return new RegisterResponse(saved.getUserId(), saved.getNickname(), saved.getTelephone(), saved.getEmail());
        } catch (DataIntegrityViolationException e) {
            // 3) Concurrent fallback (hit unique constraint)
            throw new EmailOrPhoneAlreadyUsedException("Email or telephone already used");
        }
    }
}
