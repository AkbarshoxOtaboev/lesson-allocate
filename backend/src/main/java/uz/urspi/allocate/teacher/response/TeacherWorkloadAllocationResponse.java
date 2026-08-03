package uz.urspi.allocate.teacher.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.urspi.allocate.subject.enums.Semester;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherWorkloadAllocationResponse {

    private Long allocationId;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private String departmentName;
    private Semester semester;
    private Integer courseYear;
    private Integer lectureHours;
    private Integer practicalHours;
    private Integer labHours;
    private Integer seminarHours;
    private Integer ratingHours;
    private Integer totalHours;
    private Integer independentHours;
    private Integer totalSubjectHours;
    private Double credit;
    private Integer groupCount;
    private Integer studentCount;
}
