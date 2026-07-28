package uz.urspi.allocate.subject.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.urspi.allocate.subject.enums.EducationLanguage;
import uz.urspi.allocate.subject.enums.EducationType;
import uz.urspi.allocate.subject.enums.Semester;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubjectRequest {

    @NotNull(message = "Department is required")
    private Long departmentId;

    private Long academicYearId;

    @NotNull(message = "Direction is required")
    private Long directionId;

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Semester is required")
    private Semester semester;

    @NotNull(message = "Education type is required")
    private EducationType educationType;

    @NotNull(message = "Education language is required")
    private EducationLanguage educationLanguage;

    @NotNull(message = "Total subject hours is required")
    @Min(1)
    private Integer totalSubjectHours;

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
    private Integer ratingHours;

    @Min(0)
    private Integer groupCount;

    @Min(0)
    private Integer studentCount;
}
