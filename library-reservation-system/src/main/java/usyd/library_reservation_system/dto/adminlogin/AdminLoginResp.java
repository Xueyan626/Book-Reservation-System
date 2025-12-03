package usyd.library_reservation_system.library_reservation_system.dto.adminlogin;

/**
 * Unified admin login response
 * Step-1: message returns challengeToken (client saves for step 2)
 * Step-2: message returns "successfully login!"
 */
public record AdminLoginResp(
        Integer adminId,
        String message
) {}
