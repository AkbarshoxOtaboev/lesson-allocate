package uz.urspi.allocate.teacher.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeacherWorkloadSummaryResponse {
    private Long id;
    private String name;
    private String fullName;
    private Long departmentId;
    private String departmentName;
    private Long facultyId;
    private String facultyName;
    private Double stavka;
    private Integer subjectCount;
    private Integer lectureHours;
    private Integer practicalHours;
    private Integer labHours;
    private Integer seminarHours;
    private Integer ratingHours;
    private Integer independentHours;
    private Integer totalHours;
    private Integer groupCount;
    private Integer studentCount;
    private String loadLabel;
}
