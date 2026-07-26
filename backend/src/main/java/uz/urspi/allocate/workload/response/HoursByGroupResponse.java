package uz.urspi.allocate.workload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoursByGroupResponse {

    private Long id;
    private String name;
    private Long facultyId;
    private String facultyName;
    private Integer totalHours;
    private Integer allocatedHours;
    private Integer unallocatedHours;
}
