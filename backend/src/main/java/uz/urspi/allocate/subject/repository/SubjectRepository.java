package uz.urspi.allocate.subject.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.urspi.allocate.subject.entity.Subject;
import uz.urspi.allocate.subject.enums.Semester;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    @EntityGraph(attributePaths = "groups")
    Optional<Subject> findWithGroupsById(Long id);

    List<Subject> findByDepartment_Id(Long departmentId);

    List<Subject> findByDepartment_Faculty_Id(Long facultyId);

    List<Subject> findBySemester(Semester semester);

    List<Subject> findByDepartment_IdAndSemester(Long departmentId, Semester semester);

    List<Subject> findByDepartment_Faculty_IdAndSemester(Long facultyId, Semester semester);
}
