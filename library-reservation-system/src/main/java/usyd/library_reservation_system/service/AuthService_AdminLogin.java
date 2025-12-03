package usyd.library_reservation_system.library_reservation_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usyd.library_reservation_system.library_reservation_system.dto.AdminPwdLoginReq;
import usyd.library_reservation_system.library_reservation_system.dto.adminlogin.AdminLoginResp;
import usyd.library_reservation_system.library_reservation_system.model.AdminEntity;
import usyd.library_reservation_system.library_reservation_system.repository.AdminRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService_AdminLogin {

    private final AdminRepository adminRepository;

    // Keep the same time format as before (used elsewhere)
    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    /**
     * Admin single-step login: email + plaintext password match to pass.
     * No hashing, no verification code.
     */
    @Transactional
    public AdminLoginResp pwdLogin(AdminPwdLoginReq req) {
        String email = req.getEmail() == null ? "" : req.getEmail().trim().toLowerCase();

        AdminEntity admin = adminRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        // Direct plaintext comparison (as per your latest requirement)
        if (!admin.getPassword().equals(req.getPassword())) {
            String now = LocalDateTime.now().format(TS_FMT);
            admin.setLoginLog(now + " login failed");
            adminRepository.save(admin);
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Write success log
        String now = LocalDateTime.now().format(TS_FMT);
        admin.setLoginLog(now + " login successful");
        adminRepository.save(admin);

        // Reuse existing response body
        return new AdminLoginResp(admin.getAdministratorId(), "successfully login!");
    }
}
