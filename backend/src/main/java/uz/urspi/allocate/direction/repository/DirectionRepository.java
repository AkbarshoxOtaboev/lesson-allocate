package uz.urspi.allocate.direction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.urspi.allocate.direction.entity.Direction;

import java.util.Optional;

public interface DirectionRepository extends JpaRepository<Direction, Long> {
    boolean existsByDirectionCodeIgnoreCaseAndIdNot(String directionCode, Long id);
    boolean existsByDirectionCodeIgnoreCase(String directionCode);
    Optional<Direction> findByHemisId(Long hemisId);
    Optional<Direction> findByDirectionCodeIgnoreCase(String directionCode);
}
