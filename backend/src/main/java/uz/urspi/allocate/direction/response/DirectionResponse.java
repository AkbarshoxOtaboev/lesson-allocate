package uz.urspi.allocate.direction.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.urspi.allocate.common.enums.EntityStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectionResponse {

    private Long id;
    private String name;
    private String directionCode;
    private String directionName;
    private Long hemisId;
    private Boolean hemisActive;
    private EntityStatus status;
}
