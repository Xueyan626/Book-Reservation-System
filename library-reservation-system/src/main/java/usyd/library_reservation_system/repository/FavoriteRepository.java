package usyd.library_reservation_system.library_reservation_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import usyd.library_reservation_system.library_reservation_system.model.favorite.Favorite;
import usyd.library_reservation_system.library_reservation_system.model.favorite.FavoriteId;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {
    List<Favorite> findByIdUserId(Integer userId);
}

