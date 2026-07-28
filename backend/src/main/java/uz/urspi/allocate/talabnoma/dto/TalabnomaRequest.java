package uz.urspi.allocate.talabnoma.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import uz.urspi.allocate.subject.enums.Semester;

@Getter
@Setter
public class TalabnomaRequest {

    @NotNull
    private Long toDepartmentId;

    @NotBlank
    private String subjectName;

    private String subjectCode;

    private Long academicYearId;

    private Semester semester = Semester.AUTUMN;

    @Min(0)
    private Integer lectureHours = 0;

    @Min(0)
    private Integer practicalHours = 0;

    @Min(0)
    private Integer labHours = 0;

    @Min(0)
    private Integer seminarHours = 0;

    @Min(0)
    private Integer ratingHours = 0;

    private String note;
}
