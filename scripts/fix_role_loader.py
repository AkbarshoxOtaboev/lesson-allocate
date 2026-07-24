from pathlib import Path

Path(r"e:\vibe\lesson-allocate\backend\src\main\java\uz\urspi\allocate\config\RoleDataLoader.java").write_text(
    r'''package uz.urspi.allocate.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uz.urspi.allocate.permission.entity.Permission;
import uz.urspi.allocate.permission.repository.PermissionRepository;
import uz.urspi.allocate.role.entity.Role;
import uz.urspi.allocate.role.repository.RoleRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class RoleDataLoader implements CommandLineRunner {

    private static final String SUPER_ADMIN = "SUPER_ADMIN";
    private static final String ADMIN = "ADMIN";

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {
        List<Permission> allPermissions = permissionRepository.findAll();
        Set<Permission> permissionSet = new HashSet<>(allPermissions);

        createOrSyncRole(SUPER_ADMIN, permissionSet);
        createOrSyncRole(ADMIN, permissionSet);
    }

    private void createOrSyncRole(String name, Set<Permission> permissions) {
        Role role = roleRepository.findByName(name).orElse(null);
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
''',
    encoding="utf-8",
)
print("ok")
