package usyd.library_reservation_system.library_reservation_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "administrator")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "administrator_id")
    private Integer administratorId;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    // 当前阶段是明文/可读密码
    @Column(name = "password", nullable = false, length = 128)
    private String password;

    @Lob
    @Column(name = "login_log", columnDefinition = "TEXT")
    private String loginLog;
}
