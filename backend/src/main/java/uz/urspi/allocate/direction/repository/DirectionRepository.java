package uz.urspi.allocate.direction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.urspi.allocate.direction.entity.Direction;

public interface DirectionRepository extends JpaRepository<Direction, Long> {
    boolean existsByDirectionCodeIgnoreCaseAndIdNot(String directionCode, Long id);
    boolean existsByDirectionCodeIgnoreCase(String directionCode);
}
