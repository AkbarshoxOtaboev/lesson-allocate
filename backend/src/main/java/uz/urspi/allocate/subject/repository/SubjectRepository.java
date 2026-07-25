package uz.urspi.allocate.subject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.urspi.allocate.subject.entity.Subject;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findByDepartment_Id(Long departmentId);

    List<Subject> findByDepartment_Faculty_Id(Long facultyId);
}
