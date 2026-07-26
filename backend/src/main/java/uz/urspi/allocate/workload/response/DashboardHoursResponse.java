package uz.urspi.allocate.workload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardHoursResponse {

    private Integer totalHours;
    private Integer allocatedHours;
    private Integer unallocatedHours;

    @Builder.Default
    private List<HoursByGroupResponse> byFaculty = new ArrayList<>();

    @Builder.Default
    private List<HoursByGroupResponse> byDepartment = new ArrayList<>();
}
