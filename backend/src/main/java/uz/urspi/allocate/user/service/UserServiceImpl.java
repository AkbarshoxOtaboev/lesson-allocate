package uz.urspi.allocate.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import uz.urspi.allocate.audit.annotation.Auditable;
import uz.urspi.allocate.common.enums.AuditAction;
import uz.urspi.allocate.common.enums.EntityStatus;
import uz.urspi.allocate.common.exception.BadRequestException;
import uz.urspi.allocate.common.exception.ResourceNotFoundException;
import uz.urspi.allocate.common.util.SecurityUtils;
import uz.urspi.allocate.department.entity.Department;
import uz.urspi.allocate.department.repository.DepartmentRepository;
import uz.urspi.allocate.faculty.entity.Faculty;
import uz.urspi.allocate.faculty.repository.FacultyRepository;
import uz.urspi.allocate.role.entity.Role;
import uz.urspi.allocate.role.repository.RoleRepository;
import uz.urspi.allocate.storage.StorageService;
import uz.urspi.allocate.user.dto.ProfileUpdateRequest;
import uz.urspi.allocate.user.dto.UserRequest;
import uz.urspi.allocate.user.entity.User;
import uz.urspi.allocate.user.mapper.UserMapper;
import uz.urspi.allocate.user.repository.UserRepository;
import uz.urspi.allocate.user.response.UserResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;

    @Override
    @Auditable(entity = "User", action = AuditAction.CREATE)
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already taken: " + request.getUsername());
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .roles(resolveRoles(request.getRoleIds()))
                .build();

        applyOrg(user, request.getFacultyId(), request.getDepartmentId());

        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            user.setProfileImage(storageService.save(request.getProfileImage()));
        }
        user.setCreatedUsername(SecurityUtils.getCurrentUsername());

        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return UserMapper.toResponseList(userRepository.findAllWithDetails());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserMapper.toResponse(getUserOrThrow(id));
    }

    @Override
    @Auditable(entity = "User", action = AuditAction.UPDATE)
    public UserResponse update(Long id, UserRequest request) {
        User user = getUserOrThrow(id);

        if (StringUtils.hasText(request.getUsername()) && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BadRequestException("Username already taken: " + request.getUsername());
            }
            user.setUsername(request.getUsername());
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (!CollectionUtils.isEmpty(request.getRoleIds())) {
            user.setRoles(resolveRoles(request.getRoleIds()));
        }
        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            user.setProfileImage(storageService.save(request.getProfileImage()));
        }

        applyOrg(user, request.getFacultyId(), request.getDepartmentId());

        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Auditable(entity = "User", action = AuditAction.DELETE)
    public void delete(Long id) {
        User user = getUserOrThrow(id);
        user.softDelete();
        userRepository.save(user);
    }

    @Override
    @Auditable(entity = "User", action = AuditAction.UPDATE)
    public UserResponse changeStatus(Long id) {
        User user = getUserOrThrow(id);
        user.setStatus(user.getStatus() == EntityStatus.ACTIVE ? EntityStatus.DISABLED : EntityStatus.ACTIVE);
        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentProfile() {
        return UserMapper.toResponse(requireCurrentUser());
    }

    @Override
    @Auditable(entity = "User", action = AuditAction.UPDATE)
    public UserResponse updateCurrentProfile(ProfileUpdateRequest request) {
        User user = requireCurrentUser();
        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null);
        }
        if (request.getBio() != null) {
            user.setBio(StringUtils.hasText(request.getBio()) ? request.getBio().trim() : null);
        }
        if (request.getCountry() != null) {
            user.setCountry(StringUtils.hasText(request.getCountry()) ? request.getCountry().trim() : null);
        }
        if (request.getCity() != null) {
            user.setCity(StringUtils.hasText(request.getCity()) ? request.getCity().trim() : null);
        }
        if (request.getRegion() != null) {
            user.setRegion(StringUtils.hasText(request.getRegion()) ? request.getRegion().trim() : null);
        }
        if (request.getPostalCode() != null) {
            user.setPostalCode(StringUtils.hasText(request.getPostalCode()) ? request.getPostalCode().trim() : null);
        }
        if (request.getTaxId() != null) {
            user.setTaxId(StringUtils.hasText(request.getTaxId()) ? request.getTaxId().trim() : null);
        }
        return UserMapper.toResponse(userRepository.save(user));
    }

    private User requireCurrentUser() {
        User current = SecurityUtils.getCurrentUser();
        if (current == null || current.getId() == null) {
            throw new BadRequestException("Autentifikatsiya talab qilinadi");
        }
        return getUserOrThrow(current.getId());
    }

    private void applyOrg(User user, Long facultyId, Long departmentId) {
        Department department = null;
        if (departmentId != null) {
            department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Department", departmentId));
        }
        user.setDepartment(department);

        Faculty faculty = null;
        if (facultyId != null) {
            faculty = facultyRepository.findById(facultyId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Faculty", facultyId));
        } else if (department != null) {
            faculty = department.getFaculty();
        }
        user.setFaculty(faculty);

        if (department != null && faculty != null
                && department.getFaculty() != null
                && !department.getFaculty().getId().equals(faculty.getId())) {
            throw new BadRequestException("Kafedra tanlangan fakultetga tegishli emas");
        }
    }

    private Set<Role> resolveRoles(List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return new HashSet<>();
        }
        Set<Role> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            roles.add(roleRepository.findById(roleId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Role", roleId)));
        }
        return roles;
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }
}
