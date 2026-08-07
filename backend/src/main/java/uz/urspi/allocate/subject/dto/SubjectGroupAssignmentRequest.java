package uz.urspi.allocate.subject.dto;

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
public class SubjectGroupAssignmentRequest {

    @NotNull(message = "Group is required")
    private Long groupId;

    @Min(0)
    private Integer studentCount;
}
