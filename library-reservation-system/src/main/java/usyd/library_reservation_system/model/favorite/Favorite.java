package usyd.library_reservation_system.library_reservation_system.model.favorite;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorite")
@lombok.Data @lombok.Builder
@Getter @Setter
@lombok.NoArgsConstructor @lombok.AllArgsConstructor
public class Favorite {

    @EmbeddedId
    private FavoriteId id;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
}
