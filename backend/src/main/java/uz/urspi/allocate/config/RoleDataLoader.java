package uz.urspi.allocate.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.urspi.allocate.permission.entity.Permission;
import uz.urspi.allocate.permission.repository.PermissionRepository;
import uz.urspi.allocate.role.entity.Role;
import uz.urspi.allocate.role.repository.RoleRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class RoleDataLoader implements CommandLineRunner {

    private static final String SUPER_ADMIN = "SUPER_ADMIN";
    private static final String ADMIN = "ADMIN";
    private static final String DEKAN = "DEKAN";
    private static final String KAFEDRA = "KAFEDRA";

    private static final Set<String> DEKAN_PERMISSIONS = Set.of(
            "FACULTY_VIEW",
            "DEPARTMENT_VIEW",
            "DIRECTION_VIEW",
            "TEACHER_VIEW",
            "SUBJECT_VIEW",
            "WORKLOAD_VIEW",
            "WORKLOAD_CREATE",
            "WORKLOAD_EDIT",
            "ACADEMIC_YEAR_VIEW",
            "GROUP_VIEW",
            "TALABNOMA_VIEW",
            "TALABNOMA_CREATE",
            "TALABNOMA_EDIT"
    );

    private static final Set<String> KAFEDRA_PERMISSIONS = Set.of(
            "DEPARTMENT_VIEW",
            "DIRECTION_VIEW",
            "TEACHER_VIEW",
            "SUBJECT_VIEW",
            "WORKLOAD_VIEW",
            "WORKLOAD_CREATE",
            "WORKLOAD_EDIT",
            "ACADEMIC_YEAR_VIEW",
            "GROUP_VIEW",
            "TALABNOMA_VIEW",
            "TALABNOMA_EDIT"
    );

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        List<Permission> allPermissions = permissionRepository.findAll();
        Set<Permission> permissionSet = new HashSet<>(allPermissions);

        createOrSyncRole(SUPER_ADMIN, permissionSet);
        createOrSyncRole(ADMIN, permissionSet);
        createOrSyncRole(DEKAN, filterPermissions(allPermissions, DEKAN_PERMISSIONS));
        createOrSyncRole(KAFEDRA, filterPermissions(allPermissions, KAFEDRA_PERMISSIONS));
    }

    private Set<Permission> filterPermissions(List<Permission> all, Set<String> names) {
        return all.stream()
                .filter(p -> names.contains(p.getName()))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private void createOrSyncRole(String name, Set<Permission> permissions) {
        Role role = roleRepository.findByNameWithPermissions(name).orElse(null);
        if (role == null) {
            role = Role.builder()
                    .name(name)
                    .permissions(new HashSet<>(permissions))
                    .build();
            role.setCreatedUsername("system");
            roleRepository.save(role);
            log.info("RoleDataLoader: created role {}", name);
            return;
        }
        if (role.getPermissions() == null) {
            role.setPermissions(new HashSet<>());
        }
        int before = role.getPermissions().size();
        role.getPermissions().addAll(permissions);
        if (role.getPermissions().size() > before) {
            roleRepository.save(role);
            log.info("RoleDataLoader: synced permissions to {}", name);
        }
    }
}
