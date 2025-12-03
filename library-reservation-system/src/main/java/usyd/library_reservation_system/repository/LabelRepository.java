package usyd.library_reservation_system.library_reservation_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import usyd.library_reservation_system.library_reservation_system.model.Label;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabelRepository extends JpaRepository<Label, Integer> {

    // Find by label name (exact match)
    Optional<Label> findByLabelName(String labelName);

    // Fuzzy search by label name
    List<Label> findByLabelNameContaining(String labelName);

    // Check if label name exists (for uniqueness validation)
    boolean existsByLabelName(String labelName);

    // Get all labels sorted by creation time
    @Query("SELECT l FROM Label l ORDER BY l.createDate DESC")
    List<Label> findAllOrderByCreateDateDesc();

    // Get all labels sorted by label name
    @Query("SELECT l FROM Label l ORDER BY l.labelName ASC")
    List<Label> findAllOrderByLabelNameAsc();
}