package usyd.library_reservation_system.library_reservation_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "label")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "label_id")
    private Integer labelId;

    @Column(name = "label_name", nullable = false, length = 100, unique = true)
    private String labelName;
}
