package uz.urspi.allocate.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.urspi.allocate.common.exception.ApiException;
import uz.urspi.allocate.role.entity.Role;
import uz.urspi.allocate.role.repository.RoleRepository;
import uz.urspi.allocate.user.entity.User;
import uz.urspi.allocate.user.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class UserDataLoader implements CommandLineRunner {

    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByUsername(DEFAULT_USERNAME)) {
            return;
        }
        Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                .orElseThrow(() -> new ApiException("SUPER_ADMIN role not found", HttpStatus.INTERNAL_SERVER_ERROR));

        Set<Role> roles = new HashSet<>();
        roles.add(superAdminRole);

        User admin = User.builder()
                .username(DEFAULT_USERNAME)
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .fullName("System Administrator")
                .roles(roles)
                .build();
        admin.setCreatedUsername("system");

        userRepository.save(admin);
        log.info("UserDataLoader: default admin user created (username: {})", DEFAULT_USERNAME);
    }
}