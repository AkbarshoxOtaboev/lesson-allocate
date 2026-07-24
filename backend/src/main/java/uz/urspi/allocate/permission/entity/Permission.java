package uz.urspi.allocate.permission.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.urspi.allocate.common.entity.BaseEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "permissions", uniqueConstraints = @jakarta.persistence.UniqueConstraint(columnNames = "name"))
@SQLRestriction("status <> 'DELETED'")
public class Permission extends BaseEntity {

    @EqualsAndHashCode.Include
    private String name;
}
