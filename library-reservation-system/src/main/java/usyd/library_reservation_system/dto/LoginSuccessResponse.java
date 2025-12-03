package usyd.library_reservation_system.library_reservation_system.dto;

public record LoginSuccessResponse(
        String message,
        Integer userId,
        String nickname,
        String email
) {}
