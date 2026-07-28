package uz.urspi.allocate.direction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DirectionRequest {

    @NotBlank(message = "Direction code is required")
    private String directionCode;

    @NotBlank(message = "Direction name is required")
    private String directionName;
}
