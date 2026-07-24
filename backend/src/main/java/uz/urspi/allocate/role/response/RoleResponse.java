package uz.urspi.allocate.role.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.urspi.allocate.common.enums.EntityStatus;
import uz.urspi.allocate.permission.response.PermissionResponse;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    private Long id;
    private String name;
    private EntityStatus status;
    private List<PermissionResponse> permissions;
}
