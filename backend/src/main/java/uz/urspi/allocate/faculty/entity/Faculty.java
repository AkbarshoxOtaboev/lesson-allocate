package uz.urspi.allocate.faculty.entity;

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
@Table(name = "faculties")
@SQLRestriction("status <> 'DELETED'")
public class Faculty extends BaseEntity {

    @EqualsAndHashCode.Include
    private String name;

    @Column(unique = true)
    private Long hemisId;

    private String code;

    private Boolean hemisActive;

    private String structureTypeCode;

    private Long parentHemisId;
}
