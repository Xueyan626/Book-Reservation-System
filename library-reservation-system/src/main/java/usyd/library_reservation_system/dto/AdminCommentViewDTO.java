package usyd.library_reservation_system.library_reservation_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AdminCommentViewDTO {
    private Integer commentId;
    private String bookName;
    private String userName;
    private String content;
    private LocalDateTime createDate;
    private Byte status;
}

