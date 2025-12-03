package usyd.library_reservation_system.library_reservation_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usyd.library_reservation_system.library_reservation_system.dto.AdminActionResponse;
import usyd.library_reservation_system.library_reservation_system.dto.AdminUserSummaryDTO;
import usyd.library_reservation_system.library_reservation_system.model.UserEntity;
import usyd.library_reservation_system.library_reservation_system.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 1) Fuzzy search by nickname
    public List<AdminUserSummaryDTO> searchByNickname(String nickname) {
        return userRepository.findByNicknameContainingIgnoreCase(nickname).stream()
                .map(u -> AdminUserSummaryDTO.builder()
                        .userId(u.getUserId())
                        .nickname(u.getNickname())
                        .email(u.getEmail())
                        .telephone(u.getTelephone())
                        .isActive(u.getIsActive())
                        .build())
                .toList();
    }

    // 2) Reset to default password (stored as hash)
    public AdminActionResponse resetPasswordToDefault(Integer userId, String defaultRawPassword) {
        UserEntity u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + userId));
        u.setPasswordHash(passwordEncoder.encode(defaultRawPassword));
        userRepository.save(u);

        return AdminActionResponse.builder()
                .userId(userId)
                .message("Password reset to default at " + LocalDateTime.now())
                .build();
    }

    // Ban user
    public void banUser(Integer userId){
        UserEntity u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + userId));
        u.setIsActive(false);
        userRepository.save(u);
    }

    // Unban user
    public void unbanUser(Integer userId){
        UserEntity u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + userId));
        u.setIsActive(true);
        userRepository.save(u);
    }
}
