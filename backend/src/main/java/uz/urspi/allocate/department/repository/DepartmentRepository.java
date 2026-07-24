package uz.urspi.allocate.department.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.urspi.allocate.department.entity.Department;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByHemisId(Long hemisId);
}
