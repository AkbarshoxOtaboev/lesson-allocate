package uz.urspi.allocate.permission.mapper;

import lombok.experimental.UtilityClass;
import uz.urspi.allocate.permission.entity.Permission;
import uz.urspi.allocate.permission.response.PermissionResponse;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class PermissionMapper {

    public PermissionResponse toResponse(Permission permission) {
        if (permission == null) {
            return null;
        }
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .status(permission.getStatus())
                .build();
    }

    public List<PermissionResponse> toResponseList(Collection<Permission> permissions) {
        return permissions.stream().map(PermissionMapper::toResponse).collect(Collectors.toList());
    }
}
