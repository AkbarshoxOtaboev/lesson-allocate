package uz.urspi.allocate.role.mapper;

import lombok.experimental.UtilityClass;
import uz.urspi.allocate.permission.mapper.PermissionMapper;
import uz.urspi.allocate.role.entity.Role;
import uz.urspi.allocate.role.response.RoleResponse;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class RoleMapper {

    public RoleResponse toResponse(Role role) {
        return toResponse(role, true);
    }

    public RoleResponse toResponse(Role role, boolean includePermissions) {
        if (role == null) {
            return null;
        }
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .status(role.getStatus())
                .permissions(includePermissions && role.getPermissions() != null
                        ? PermissionMapper.toResponseList(role.getPermissions())
                        : Collections.emptyList())
                .build();
    }

    public List<RoleResponse> toResponseList(Collection<Role> roles) {
        return roles.stream().map(role -> RoleMapper.toResponse(role, true)).collect(Collectors.toList());
    }
}
