package uz.urspi.allocate.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.urspi.allocate.group.entity.Group;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByHemisId(Long hemisId);

    List<Group> findByDepartment_Id(Long departmentId);

    List<Group> findByFaculty_Id(Long facultyId);

    List<Group> findByIdIn(List<Long> ids);
}
