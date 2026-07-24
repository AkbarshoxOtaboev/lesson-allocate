package uz.urspi.allocate.department.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.urspi.allocate.common.entity.BaseEntity;
import uz.urspi.allocate.faculty.entity.Faculty;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "departments")
@SQLRestriction("status <> 'DELETED'")
public class Department extends BaseEntity {

    @EqualsAndHashCode.Include
    private String name;

    @Column(unique = true)
    private Long hemisId;

    private String code;

    private Boolean hemisActive;

    private String structureTypeCode;

    private Long parentHemisId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;
}
