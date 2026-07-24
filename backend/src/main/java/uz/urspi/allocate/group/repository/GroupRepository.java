package uz.urspi.allocate.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.urspi.allocate.group.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {
}
