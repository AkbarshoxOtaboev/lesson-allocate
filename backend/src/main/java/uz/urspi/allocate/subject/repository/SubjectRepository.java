package uz.urspi.allocate.subject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.urspi.allocate.subject.entity.Subject;
import uz.urspi.allocate.subject.enums.Semester;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findByDepartment_Id(Long departmentId);

    List<Subject> findByDepartment_Faculty_Id(Long facultyId);

    List<Subject> findBySemester(Semester semester);

    List<Subject> findByDepartment_IdAndSemester(Long departmentId, Semester semester);

    List<Subject> findByDepartment_Faculty_IdAndSemester(Long facultyId, Semester semester);
}
