package uz.urspi.allocate.role.service;

import uz.urspi.allocate.permission.response.PermissionResponse;
import uz.urspi.allocate.role.dto.RoleRequest;
import uz.urspi.allocate.role.response.RoleResponse;

import java.util.List;

public interface RoleService {

    RoleResponse create(RoleRequest request);

    List<RoleResponse> findAll();

    RoleResponse findById(Long id);

    RoleResponse update(Long id, RoleRequest request);

    void delete(Long id);

    RoleResponse addPermission(Long roleId, Long permissionId);

    RoleResponse removePermission(Long roleId, Long permissionId);

    List<PermissionResponse> getPermissions(Long roleId);

    List<PermissionResponse> getAllPermissions();
}
