package uz.urspi.allocate.subject.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import uz.urspi.allocate.academicyear.entity.AcademicYear;
import uz.urspi.allocate.common.entity.BaseEntity;
import uz.urspi.allocate.department.entity.Department;
import uz.urspi.allocate.direction.entity.Direction;
import uz.urspi.allocate.subject.enums.EducationLanguage;
import uz.urspi.allocate.subject.enums.EducationType;
import uz.urspi.allocate.subject.enums.Semester;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "subjects")
@SQLRestriction("status <> 'DELETED'")
public class Subject extends BaseEntity {

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    @EqualsAndHashCode.Include
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direction_id")
    private Direction direction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Semester semester = Semester.AUTUMN;

    @Enumerated(EnumType.STRING)
    @Column
    @Builder.Default
    private EducationType educationType = EducationType.KUNDUZGI;

    @Enumerated(EnumType.STRING)
    @Column
    @Builder.Default
    private EducationLanguage educationLanguage = EducationLanguage.UZB;

    /** Umumiy fan soati (masalan 1200) */
    @Builder.Default
    private Integer totalSubjectHours = 0;

    @Builder.Default
    private Integer lectureHours = 0;

    @Builder.Default
    private Integer practicalHours = 0;

    @Builder.Default
    private Integer labHours = 0;

    @Builder.Default
    private Integer seminarHours = 0;

    @Builder.Default
    private Integer independentStudyHours = 0;

    /** Reyting soati */
    @Builder.Default
    private Integer ratingHours = 0;

    @Builder.Default
    private Integer groupCount = 0;

    @Builder.Default
    private Integer studentCount = 0;

    /** 1–5 kurs */
    @Builder.Default
    private Integer courseYear = 1;
}
