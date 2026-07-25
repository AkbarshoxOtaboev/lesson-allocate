package uz.urspi.allocate.subject.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.urspi.allocate.common.enums.EntityStatus;

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
    private Integer lectureHours;
    private Integer practicalHours;
    private Integer labHours;
    private Integer seminarHours;
    private Integer independentStudyHours;
    /** Maruza + amaliy + lab + seminar */
    private Integer totalHours;
    /** Jami + mustaqil ta'lim */
    private Integer overallHours;
    /** Umumiy fan soati / 3 */
    private Double credit;
    private Integer groupCount;
    private Integer studentCount;
}
