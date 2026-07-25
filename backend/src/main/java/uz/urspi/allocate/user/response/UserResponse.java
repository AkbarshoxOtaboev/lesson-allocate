package uz.urspi.allocate.user.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.urspi.allocate.common.enums.EntityStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String fullName;
    private String phone;
    private String profileImage;
    private LocalDateTime lastLogin;
    private EntityStatus status;
    private List<RoleSummary> roles;
    private Long facultyId;
    private String facultyName;
    private Long departmentId;
    private String departmentName;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleSummary {
        private Long id;
        private String name;
    }
}
