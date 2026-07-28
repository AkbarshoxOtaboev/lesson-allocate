package uz.urspi.allocate.direction.entity;

import jakarta.persistence.Column;
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
@Table(name = "directions")
@SQLRestriction("status <> 'DELETED'")
public class Direction extends BaseEntity {

    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String directionCode;

    @Column(nullable = false)
    private String directionName;
}
