package uz.urspi.allocate.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uz.urspi.allocate.common.enums.ActionType;
import uz.urspi.allocate.common.enums.ResourceType;
import uz.urspi.allocate.permission.entity.Permission;
import uz.urspi.allocate.permission.repository.PermissionRepository;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class PermissionsDataLoader implements CommandLineRunner {

    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {
        int created = 0;
        for (ResourceType resource : ResourceType.values()) {
            for (ActionType action : ActionType.values()) {
                String name = resource.name() + "_" + action.name();
                if (!permissionRepository.existsByName(name)) {
                    Permission permission = Permission.builder().name(name).build();
                    permission.setCreatedUsername("system");
                    permissionRepository.save(permission);
                    created++;
                }
            }
        }
        log.info("PermissionsDataLoader: {} new permissions created", created);
    }
}
