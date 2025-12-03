package usyd.library_reservation_system.library_reservation_system.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "comment")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer commentId;

    private Integer bookId;

    @Column(nullable = false)
    private String content;


    @CreationTimestamp
    @Column(name = "create_date", updatable = false)
    private LocalDateTime createDate;

    private Integer authorId;

    private Byte authorType; // 0=用户, 1=管理员

    private Byte status;     // 0=待审核, 1=通过
}
