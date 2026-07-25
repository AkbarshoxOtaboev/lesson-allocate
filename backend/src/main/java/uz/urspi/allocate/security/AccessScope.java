package uz.urspi.allocate.security;

import lombok.Getter;
import uz.urspi.allocate.common.util.SecurityUtils;
import uz.urspi.allocate.department.entity.Department;
import uz.urspi.allocate.faculty.entity.Faculty;
import uz.urspi.allocate.user.entity.User;

/**
 * Joriy foydalanuvchi uchun tashkiliy ko'rinish chegarasi.
 * SUPER_ADMIN / ADMIN — cheklovsiz; DEKAN — fakultet; KAFEDRA — kafedra.
 */
@Getter
public final class AccessScope {

    private final Long facultyId;
    private final Long departmentId;
    private final boolean unrestricted;

    private AccessScope(Long facultyId, Long departmentId, boolean unrestricted) {
        this.facultyId = facultyId;
        this.departmentId = departmentId;
        this.unrestricted = unrestricted;
    }

    public static AccessScope unrestricted() {
        return new AccessScope(null, null, true);
    }

    public static AccessScope ofCurrentUser() {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) {
            return unrestricted();
        }
        if (user.hasRole("SUPER_ADMIN") || user.hasRole("ADMIN")) {
            return unrestricted();
        }

        Long facultyId = null;
        Long departmentId = null;

        Faculty faculty = user.getFaculty();
        if (faculty != null) {
            facultyId = faculty.getId();
        }

        Department department = user.getDepartment();
        if (department != null) {
            departmentId = department.getId();
            if (facultyId == null && department.getFaculty() != null) {
                facultyId = department.getFaculty().getId();
            }
        }

        if (user.hasRole("KAFEDRA") && departmentId != null) {
            return new AccessScope(facultyId, departmentId, false);
        }
        if (user.hasRole("DEKAN") && facultyId != null) {
            return new AccessScope(facultyId, null, false);
        }

        // Rol bor lekin org biriktirilmagan — bo'sh natija
        if (user.hasRole("DEKAN") || user.hasRole("KAFEDRA")) {
            return new AccessScope(-1L, user.hasRole("KAFEDRA") ? -1L : null, false);
        }

        return unrestricted();
    }

    public Long resolveFacultyId(Long requestedFacultyId) {
        if (unrestricted) {
            return requestedFacultyId;
        }
        return facultyId;
    }

    public Long resolveDepartmentId(Long requestedDepartmentId) {
        if (unrestricted) {
            return requestedDepartmentId;
        }
        if (departmentId != null) {
            return departmentId;
        }
        return requestedDepartmentId;
    }
}
