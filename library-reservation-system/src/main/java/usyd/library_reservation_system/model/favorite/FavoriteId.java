package usyd.library_reservation_system.library_reservation_system.model.favorite;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@lombok.Data
@Getter @Setter
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@EqualsAndHashCode
public class FavoriteId implements java.io.Serializable {
    private Integer bookId;
    private Integer userId;
}
