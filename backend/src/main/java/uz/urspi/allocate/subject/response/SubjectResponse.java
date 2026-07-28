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
    private Long academicYearId;
    private String academicYearName;
    private Long directionId;
    private String directionCode;
    private String directionName;
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
    /** Maruza + amaliy + lab + seminar + reyting */
    private Integer totalHours;
    /** Auditoriy + mustaqil ta'lim */
    private Integer overallHours;
    /** Umumiy fan soati / 30 */
    private Double credit;
    private Integer groupCount;
    private Integer studentCount;
}
