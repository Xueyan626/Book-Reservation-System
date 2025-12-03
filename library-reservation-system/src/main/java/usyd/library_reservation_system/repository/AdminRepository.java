package usyd.library_reservation_system.library_reservation_system.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import usyd.library_reservation_system.library_reservation_system.model.AdminEntity;

public interface AdminRepository extends JpaRepository<AdminEntity, Integer> {
    Optional<AdminEntity> findByEmailIgnoreCase(String email);
}
