package uz.urspi.allocate.workload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherLoadResponse {

    private Long id;
    private String name;
    private String departmentName;
    private String staffPositionName;
    private Integer totalAssignedHours;
    private String loadLabel;
    private Integer existingLectureHours;
    private Integer existingSeminarHours;
    private Integer existingPracticalHours;
    private Integer existingLabHours;
    private Integer existingRatingHours;
    private List<AllocatedGroupResponse> existingGroups;
}
