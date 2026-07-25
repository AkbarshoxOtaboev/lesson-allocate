package uz.urspi.allocate.teacher.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String firstName;
    private String secondName;
    private String thirdName;
    private String employeeIdNumber;
    private Long departmentId;
    private String staffPositionName;
    private String staffPositionCode;
}
