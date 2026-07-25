package uz.urspi.allocate.academicyear.entity;

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
@Table(name = "academic_years")
@SQLRestriction("status <> 'DELETED'")
public class AcademicYear extends BaseEntity {

    /** Masalan: 2025-2026 */
    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String name;

    @Column(nullable = false)
    private Integer startYear;

    @Column(nullable = false)
    private Integer endYear;

    /** Joriy o'quv yili belgilovi */
    @Builder.Default
    private Boolean currentYear = false;
}
