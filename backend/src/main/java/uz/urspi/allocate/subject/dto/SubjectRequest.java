package uz.urspi.allocate.subject.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubjectRequest {

    @NotNull(message = "Department is required")
    private Long departmentId;

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    @Min(0)
    private Integer lectureHours;

    @Min(0)
    private Integer practicalHours;

    @Min(0)
    private Integer labHours;

    @Min(0)
    private Integer seminarHours;

    @Min(0)
    private Integer independentStudyHours;

    @Min(0)
    private Integer groupCount;

    @Min(0)
    private Integer studentCount;
}
