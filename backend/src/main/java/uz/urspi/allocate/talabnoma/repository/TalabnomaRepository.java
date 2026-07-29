package uz.urspi.allocate.talabnoma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.urspi.allocate.talabnoma.entity.Talabnoma;
import uz.urspi.allocate.talabnoma.enums.TalabnomaStatus;

import java.util.List;
import java.util.Optional;

public interface TalabnomaRepository extends JpaRepository<Talabnoma, Long> {

    Optional<Talabnoma> findByCode(String code);

    List<Talabnoma> findByToDepartment_IdOrderByCreatedAtDesc(Long departmentId);

    List<Talabnoma> findByFromFaculty_IdOrderByCreatedAtDesc(Long facultyId);

    List<Talabnoma> findByToDepartment_Faculty_IdOrderByCreatedAtDesc(Long facultyId);

    long countByToDepartment_IdAndRequestStatus(Long departmentId, TalabnomaStatus status);

    long countByRequestStatus(TalabnomaStatus status);

    Optional<Talabnoma> findByLinkedSubject_Id(Long subjectId);

    List<Talabnoma> findByLinkedSubject_IdIn(List<Long> subjectIds);

    @Query("select count(t) from Talabnoma t where t.requestStatus = :status")
    long countByStatus(@Param("status") TalabnomaStatus status);
}
