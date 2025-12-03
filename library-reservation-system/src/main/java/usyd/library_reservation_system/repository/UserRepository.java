package usyd.library_reservation_system.library_reservation_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import usyd.library_reservation_system.library_reservation_system.model.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByEmailIgnoreCase(String email);
    Optional<UserEntity> findByEmailIgnoreCaseAndIsActiveTrue(String email);

    List<UserEntity> findByNicknameContainingIgnoreCaseAndIsActiveTrue(String nickname);
    List<UserEntity> findByNicknameContainingIgnoreCase(String nickname);

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByTelephone(String telephone);

}