package uz.urspi.allocate.role.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.urspi.allocate.audit.annotation.Auditable;
import uz.urspi.allocate.common.enums.AuditAction;
import org.springframework.transaction.annotation.Transactional;
import uz.urspi.allocate.common.exception.BadRequestException;
import uz.urspi.allocate.common.exception.ResourceNotFoundException;
import uz.urspi.allocate.common.util.SecurityUtils;
import uz.urspi.allocate.permission.entity.Permission;
import uz.urspi.allocate.permission.mapper.PermissionMapper;
import uz.urspi.allocate.permission.repository.PermissionRepository;
import uz.urspi.allocate.permission.response.PermissionResponse;
import uz.urspi.allocate.role.dto.RoleRequest;
import uz.urspi.allocate.role.entity.Role;
import uz.urspi.allocate.role.mapper.RoleMapper;
import uz.urspi.allocate.role.repository.RoleRepository;
import uz.urspi.allocate.role.response.RoleResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Auditable(entity = "Role", action = AuditAction.CREATE)
    public RoleResponse create(RoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new BadRequestException("Role already exists with name: " + request.getName());
        }
        Role role = Role.builder()
                .name(request.getName())
                .build();
        role.setCreatedUsername(SecurityUtils.getCurrentUsername());
        return RoleMapper.toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> findAll() {
        return RoleMapper.toResponseList(roleRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse findById(Long id) {
        return RoleMapper.toResponse(getRoleOrThrow(id));
    }

    @Override
    @Auditable(entity = "Role", action = AuditAction.UPDATE)
    public RoleResponse update(Long id, RoleRequest request) {
        Role role = getRoleOrThrow(id);
        role.setName(request.getName());
        return RoleMapper.toResponse(roleRepository.save(role));
    }

    @Override
    @Auditable(entity = "Role", action = AuditAction.DELETE)
    public void delete(Long id) {
        Role role = getRoleOrThrow(id);
        role.softDelete();
        roleRepository.save(role);
    }

    @Override
    @Auditable(entity = "Role", action = AuditAction.UPDATE)
    public RoleResponse addPermission(Long roleId, Long permissionId) {
        Role role = getRoleOrThrow(roleId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> ResourceNotFoundException.of("Permission", permissionId));
        role.getPermissions().add(permission);
        return RoleMapper.toResponse(roleRepository.save(role));
    }

    @Override
    @Auditable(entity = "Role", action = AuditAction.UPDATE)
    public RoleResponse removePermission(Long roleId, Long permissionId) {
        Role role = getRoleOrThrow(roleId);
        role.getPermissions().removeIf(permission -> permission.getId().equals(permissionId));
        return RoleMapper.toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissions(Long roleId) {
        Role role = getRoleOrThrow(roleId);
        return PermissionMapper.toResponseList(role.getPermissions());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return PermissionMapper.toResponseList(permissionRepository.findAll());
    }

    private Role getRoleOrThrow(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Role", id));
    }
}
