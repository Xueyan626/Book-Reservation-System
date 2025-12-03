
package usyd.library_reservation_system.library_reservation_system.model;

import jakarta.persistence.*;
        import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "label")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "label_id")
    private Integer labelId;

    @Column(name = "label_name", nullable = false, length = 100, unique = true)
    private String labelName;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @PrePersist
    protected void onCreate() {
        if (createDate == null) {
            createDate = LocalDateTime.now();
        }
    }
}