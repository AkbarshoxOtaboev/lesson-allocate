package uz.urspi.allocate.academicyear.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AcademicYearRequest {

    @NotNull(message = "Start year is required")
    @Min(2000)
    @Max(2100)
    private Integer startYear;

    @NotNull(message = "End year is required")
    @Min(2000)
    @Max(2100)
    private Integer endYear;

    private Boolean currentYear;
}
