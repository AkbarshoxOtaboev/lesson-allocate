package uz.urspi.allocate.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uz.urspi.allocate.common.entity.BaseEntity;
import uz.urspi.allocate.common.enums.EntityStatus;
import uz.urspi.allocate.department.entity.Department;
import uz.urspi.allocate.faculty.entity.Faculty;
import uz.urspi.allocate.permission.entity.Permission;
import uz.urspi.allocate.role.entity.Role;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
@SQLRestriction("status <> 'DELETED'")
public class User extends BaseEntity implements UserDetails {

    @EqualsAndHashCode.Include
    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String fullName;

    private String phone;

    private String profileImage;

    private String bio;

    private String country;

    private String city;

    private String region;

    private String postalCode;

    private String taxId;

    private LocalDateTime lastLogin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public boolean hasRole(String roleName) {
        if (roles == null || roleName == null) {
            return false;
        }
        return roles.stream().anyMatch(r -> roleName.equals(r.getName()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            for (Permission permission : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(permission.getName()));
            }
        }
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return getStatus() != EntityStatus.DELETED;
    }

    @Override
    public boolean isAccountNonLocked() {
        return getStatus() != EntityStatus.DISABLED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return getStatus() == EntityStatus.ACTIVE;
    }
}
