package usyd.library_reservation_system.library_reservation_system.dto;

import lombok.Builder;
import lombok.Data;

public record RegisterResponse(
        Integer userId,
        String nickname,
        String telephone,
        String email
) {}

