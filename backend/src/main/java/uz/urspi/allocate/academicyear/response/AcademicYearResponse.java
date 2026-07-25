package uz.urspi.allocate.academicyear.response;

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
public class AcademicYearResponse {

    private Long id;
    private String name;
    private Integer startYear;
    private Integer endYear;
    private Boolean currentYear;
    private EntityStatus status;
}
