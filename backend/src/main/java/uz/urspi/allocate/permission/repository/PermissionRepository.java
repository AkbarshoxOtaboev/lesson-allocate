package uz.urspi.allocate.permission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.urspi.allocate.permission.entity.Permission;

import java.util.Optional;
import java.util.Set;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    Set<Permission> findByIdIn(Set<Long> ids);
}
