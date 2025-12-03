package usyd.library_reservation_system.library_reservation_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "`user`") // 反引号防止与 MySQL 关键字冲突
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 对应 AUTO_INCREMENT
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "telephone", nullable = false, length = 20)
    private String telephone; // DB 有唯一索引

    @Column(name = "email", nullable = false, length = 100)
    private String email;     // DB 有唯一索引

    // 数据库列名叫 password，这里用 passwordHash 来表达“这是哈希”
    @Column(name = "password", nullable = false, length = 128)
    private String passwordHash; // BCrypt 60 字符，这里留 128 更保险

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    // @Column(name = "create_time")
    // private LocalDateTime createTime;
}