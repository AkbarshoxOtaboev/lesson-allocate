package uz.urspi.allocate.workload.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadAllocateRequest {

    @NotNull
    private Long subjectId;

    @NotNull
    private Long teacherId;

    @Min(0)
    private Integer lectureHours;

    @Min(0)
    private Integer seminarHours;

    @Min(0)
    private Integer practicalHours;

    @Min(0)
    private Integer labHours;

    @Min(0)
    private Integer ratingHours;

    private String employmentStaffName;

    private Double workloadRate;

    private List<Long> groupIds = new ArrayList<>();
}
