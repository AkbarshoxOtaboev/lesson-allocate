package uz.urspi.allocate.permission.mapper;

import lombok.experimental.UtilityClass;
import uz.urspi.allocate.permission.entity.Permission;
import uz.urspi.allocate.permission.response.PermissionResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class PermissionMapper {

    private static final Map<String, String> RESOURCE_LABELS_UZ = Map.ofEntries(
            Map.entry("USER", "Foydalanuvchilar"),
            Map.entry("ROLE", "Rollar"),
            Map.entry("FACULTY", "Fakultetlar"),
            Map.entry("DEPARTMENT", "Kafedralar"),
            Map.entry("GROUP", "Guruhlar"),
            Map.entry("TEACHER", "O'qituvchilar"),
            Map.entry("SUBJECT", "Fanlar"),
            Map.entry("WORKLOAD", "Yuklamalar"),
            Map.entry("ACADEMIC_YEAR", "O'quv yillari"),
            Map.entry("EXTERNAL_TOKEN", "Tashqi tokenlar"),
            Map.entry("EMPLOYEE", "Xodimlar")
    );

    private static final Map<String, String> ACTION_LABELS_UZ = Map.ofEntries(
            Map.entry("VIEW", "ko'rish"),
            Map.entry("CREATE", "yaratish"),
            Map.entry("EDIT", "tahrirlash"),
            Map.entry("DELETE", "o'chirish")
    );

    public PermissionResponse toResponse(Permission permission) {
        if (permission == null) {
            return null;
        }
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .labelUz(toUzbekLabel(permission.getName()))
                .status(permission.getStatus())
                .build();
    }

    public List<PermissionResponse> toResponseList(Collection<Permission> permissions) {
        return permissions.stream().map(PermissionMapper::toResponse).collect(Collectors.toList());
    }

    private String toUzbekLabel(String permissionName) {
        if (permissionName == null || permissionName.isBlank()) {
            return permissionName;
        }

        int separatorIndex = permissionName.lastIndexOf('_');
        if (separatorIndex <= 0 || separatorIndex >= permissionName.length() - 1) {
            return permissionName;
        }

        String resourceKey = permissionName.substring(0, separatorIndex);
        String actionKey = permissionName.substring(separatorIndex + 1);

        String resourceLabel = RESOURCE_LABELS_UZ.getOrDefault(resourceKey, resourceKey);
        String actionLabel = ACTION_LABELS_UZ.getOrDefault(actionKey, actionKey.toLowerCase());
        return resourceLabel + " - " + actionLabel;
    }
}
