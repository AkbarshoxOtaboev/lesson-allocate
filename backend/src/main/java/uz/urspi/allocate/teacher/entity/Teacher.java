package uz.urspi.allocate.teacher.entity;

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
import uz.urspi.allocate.department.entity.Department;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "teachers")
@SQLRestriction("status <> 'DELETED'")
public class Teacher extends BaseEntity {

    @Column(unique = true)
    @EqualsAndHashCode.Include
    private Long hemisId;

    private String firstName;

    private String secondName;

    private String thirdName;

    private String fullName;

    private String shortName;

    private String employeeIdNumber;

    private String birthDate;

    private String image;

    private String genderCode;

    private String genderName;

    private Long departmentHemisId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    private String staffPositionCode;

    private String staffPositionName;

    private String employeeStatusCode;

    private String employeeStatusName;

    private String employmentFormCode;

    private String employmentFormName;

    private String employmentStaffCode;

    private String employmentStaffName;

    private String employeeTypeCode;

    private String employeeTypeName;

    private String academicRankCode;

    private String academicRankName;

    private String academicDegreeCode;

    private String academicDegreeName;
}
