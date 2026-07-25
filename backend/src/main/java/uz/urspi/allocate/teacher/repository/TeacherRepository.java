package uz.urspi.allocate.teacher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.urspi.allocate.teacher.entity.Teacher;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByHemisId(Long hemisId);

    List<Teacher> findByDepartment_Id(Long departmentId);

    List<Teacher> findByDepartment_Faculty_Id(Long facultyId);

    long countByDepartment_Id(Long departmentId);

    long countByDepartment_Faculty_Id(Long facultyId);
}
