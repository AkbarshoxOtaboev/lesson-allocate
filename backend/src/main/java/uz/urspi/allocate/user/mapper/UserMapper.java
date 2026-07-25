package uz.urspi.allocate.user.mapper;

import lombok.experimental.UtilityClass;
import uz.urspi.allocate.role.entity.Role;
import uz.urspi.allocate.user.entity.User;
import uz.urspi.allocate.user.response.UserResponse;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        List<UserResponse.RoleSummary> roleSummaries = user.getRoles() == null
                ? Collections.emptyList()
                : user.getRoles().stream()
                    .map(UserMapper::toRoleSummary)
                    .collect(Collectors.toList());

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .profileImage(user.getProfileImage())
                .lastLogin(user.getLastLogin())
                .status(user.getStatus())
                .roles(roleSummaries)
                .facultyId(user.getFaculty() != null ? user.getFaculty().getId() : null)
                .facultyName(user.getFaculty() != null ? user.getFaculty().getName() : null)
                .departmentId(user.getDepartment() != null ? user.getDepartment().getId() : null)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .build();
    }

    public List<UserResponse> toResponseList(Collection<User> users) {
        return users.stream().map(UserMapper::toResponse).collect(Collectors.toList());
    }

    private UserResponse.RoleSummary toRoleSummary(Role role) {
        return UserResponse.RoleSummary.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }
}
