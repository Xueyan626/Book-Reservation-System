package usyd.library_reservation_system.library_reservation_system.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDTO {
    private Integer commentId;
    private Integer bookId;
    private String content;
    private LocalDateTime createDate;
    private Integer authorId;
    private String authorName;  // 用户名
    private Byte authorType;  // 0=User, 1=Admin
    private Byte status;      // 0=Pending, 1=Approved
}


