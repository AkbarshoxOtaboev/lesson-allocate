package uz.urspi.allocate.subject.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.urspi.allocate.common.enums.EntityStatus;
import uz.urspi.allocate.subject.enums.EducationLanguage;
import uz.urspi.allocate.subject.enums.EducationType;
import uz.urspi.allocate.subject.enums.Semester;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResponse {

    private Long id;
    private String code;
    private String name;
    private EntityStatus status;
    private Long departmentId;
    private String departmentName;
    private Long facultyId;
    private String facultyName;
    /** Talabnoma orqali kelgan fan uchun yuborgan fakultet */
    private Long sourceFacultyId;
    private String sourceFacultyName;
    private String talabnomaCode;
    private Long academicYearId;
    private String academicYearName;
    private Long directionId;
    private String directionCode;
    private String directionName;
    /** 1–5 kurs */
    private Integer courseYear;
    private Semester semester;
    private EducationType educationType;
    private EducationLanguage educationLanguage;
    private Integer totalSubjectHours;
    private Integer lectureHours;
    private Integer practicalHours;
    private Integer labHours;
    private Integer seminarHours;
    private Integer independentStudyHours;
    private Integer ratingHours;
    /**
     * Umumiy soat = ma'ruza + amaliy + lab + seminar + mustaqil + reyting
     */
    private Integer totalHours;
    /**
     * Auditoriya soatlari = ma'ruza + amaliy + lab + seminar + reyting
     */
    private Integer auditoriumHours;
    /** @deprecated totalHours bilan bir xil — moslik uchun */
    private Integer overallHours;
    /** Umumiy fan soati / 30 */
    private Double credit;
    private Integer groupCount;
    private Integer studentCount;
    private List<SubjectGroupResponse> groups;
}
