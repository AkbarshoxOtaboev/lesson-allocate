package uz.urspi.allocate.subject.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
import uz.urspi.allocate.group.entity.Group;
import uz.urspi.allocate.subject.enums.EducationLanguage;
import uz.urspi.allocate.subject.enums.EducationType;
import uz.urspi.allocate.subject.enums.Semester;

import java.util.HashSet;
import java.util.Set;

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

    /**
     * Umumiy soat = lecture + practical + lab + seminar + independent + rating.
     * DTO orqali kelmaydi — saqlashda hisoblanadi.
     */
    @Builder.Default
    private Integer totalHours = 0;

    /**
     * Auditoriya soatlari = lecture + practical + lab + seminar + rating.
     * DTO orqali kelmaydi — saqlashda hisoblanadi.
     */
    @Builder.Default
    private Integer auditoriumHours = 0;

    @Builder.Default
    private Integer groupCount = 0;

    @Builder.Default
    private Integer studentCount = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "subject_groups",
            joinColumns = @JoinColumn(name = "subject_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    @Builder.Default
    private Set<Group> groups = new HashSet<>();

    /** 1–5 kurs */
    @Builder.Default
    private Integer courseYear = 1;

    @jakarta.persistence.PrePersist
    @jakarta.persistence.PreUpdate
    void recalculateDerivedHours() {
        int lecture = lectureHours == null ? 0 : lectureHours;
        int practical = practicalHours == null ? 0 : practicalHours;
        int lab = labHours == null ? 0 : labHours;
        int seminar = seminarHours == null ? 0 : seminarHours;
        int independent = independentStudyHours == null ? 0 : independentStudyHours;
        int rating = ratingHours == null ? 0 : ratingHours;
        this.auditoriumHours = lecture + practical + lab + seminar + rating;
        this.totalHours = this.auditoriumHours + independent;
    }
}
