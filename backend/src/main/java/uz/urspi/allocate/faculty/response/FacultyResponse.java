package uz.urspi.allocate.faculty.response;

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
public class FacultyResponse {

    private Long id;
    private String name;
    private EntityStatus status;
    private Long hemisId;
    private String code;
    private Boolean hemisActive;
    private String structureTypeCode;
    private long departmentCount;
    private long teacherCount;
}
