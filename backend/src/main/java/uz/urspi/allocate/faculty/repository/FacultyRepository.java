package uz.urspi.allocate.faculty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.urspi.allocate.faculty.entity.Faculty;

import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    Optional<Faculty> findByHemisId(Long hemisId);
}
