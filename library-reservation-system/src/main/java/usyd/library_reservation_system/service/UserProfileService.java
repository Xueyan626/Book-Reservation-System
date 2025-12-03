package usyd.library_reservation_system.library_reservation_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usyd.library_reservation_system.library_reservation_system.dto.ChangePasswordReq;
import usyd.library_reservation_system.library_reservation_system.dto.UserProfileDTO;
import usyd.library_reservation_system.library_reservation_system.model.UserEntity;
import usyd.library_reservation_system.library_reservation_system.repository.UserRepository;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileDTO getProfile(Integer userId) {
        UserEntity u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        UserProfileDTO.UserProfileDTOBuilder b = UserProfileDTO.builder()
                .userId(u.getUserId())
                .nickname(u.getNickname())
                .email(u.getEmail())
                .telephone(u.getTelephone());

        // If you added create_time column, return formatted string; otherwise ignore
        // if (u.getCreateTime() != null) {
        //     String ts = u.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        //     b.registrationTime(ts);
        // }

        return b.build();
    }

    @Transactional
    public void changePassword(Integer userId, ChangePasswordReq req) {
        UserEntity u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND:" + userId));

        // Handle null/empty values uniformly
        String current = req.getCurrentPassword() == null ? "" : req.getCurrentPassword().trim();
        String newPwd  = req.getNewPassword() == null ? "" : req.getNewPassword().trim();
        String confirm = req.getConfirmNewPassword() == null ? "" : req.getConfirmNewPassword().trim();

        // 1) Check if old password is correct
        if (!passwordEncoder.matches(current, u.getPasswordHash())) {
            throw new IllegalArgumentException("CURRENT_PASSWORD_INCORRECT");
        }

        // 2) New password must match confirmed password
        if (!newPwd.equals(confirm)) {
            throw new IllegalArgumentException("NEW_PASSWORD_MISMATCH");
        }

        // 3) New password cannot be the same as old password (optional but recommended)
        if (passwordEncoder.matches(newPwd, u.getPasswordHash())) {
            throw new IllegalArgumentException("NEW_PASSWORD_SAME_AS_OLD");
        }

        // 4) Strength validation (consistent with registration)
        boolean strong = newPwd.length() >= 8 && newPwd.length() <= 64
                && newPwd.chars().anyMatch(Character::isUpperCase)
                && newPwd.chars().anyMatch(Character::isDigit);
        if (!strong) {
            throw new IllegalArgumentException("WEAK_PASSWORD");
        }

        // 5) Generate hash and save
        u.setPasswordHash(passwordEncoder.encode(newPwd));
        // If there's an update timestamp field, can update it here:
        // u.setUpdatedAt(Instant.now());

        userRepository.save(u);
    }

}
