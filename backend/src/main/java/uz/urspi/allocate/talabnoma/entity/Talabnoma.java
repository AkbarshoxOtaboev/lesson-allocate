package uz.urspi.allocate.talabnoma.entity;

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
import uz.urspi.allocate.faculty.entity.Faculty;
import uz.urspi.allocate.subject.entity.Subject;
import uz.urspi.allocate.subject.enums.EducationLanguage;
import uz.urspi.allocate.subject.enums.EducationType;
import uz.urspi.allocate.subject.enums.Semester;
import uz.urspi.allocate.talabnoma.enums.TalabnomaStatus;
import uz.urspi.allocate.user.entity.User;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "talabnomalar")
@SQLRestriction("status <> 'DELETED'")
public class Talabnoma extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    @EqualsAndHashCode.Include
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_faculty_id", nullable = false)
    private Faculty fromFaculty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_department_id", nullable = false)
    private Department toDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(nullable = false)
    private String subjectName;

    @Column(length = 64)
    private String subjectCode;

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

    /** Umumiy fan soati (reytingdan tashqari taqsimot asosi) */
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

    @Builder.Default
    private Integer ratingHours = 0;

    /** Auditoriy soat (maruza+amaliy+lab+seminar+reyting) — taqsimlash holati uchun */
    @Builder.Default
    private Integer totalHours = 0;

    @Builder.Default
    private Integer groupCount = 0;

    @Builder.Default
    private Integer studentCount = 0;

    /** 1–5 kurs */
    @Builder.Default
    private Integer courseYear = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TalabnomaStatus requestStatus = TalabnomaStatus.NEW;

    @Column(length = 1000)
    private String note;

    @Column(length = 1000)
    private String rejectReason;

    /** Qabul qilingandan keyin kafedra faniga bog'lanadi */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_subject_id")
    private Subject linkedSubject;
}
