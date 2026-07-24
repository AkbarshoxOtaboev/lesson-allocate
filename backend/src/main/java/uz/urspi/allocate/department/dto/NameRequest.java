package uz.urspi.allocate.department.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NameRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private Long facultyId;
}
