package uz.urspi.allocate.teacher.response;

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
public class TeacherResponse {

    private Long id;
    /** Catalog UI uchun to'liq ism */
    private String name;
    private String firstName;
    private String secondName;
    private String thirdName;
    private String fullName;
    private String shortName;
    private String employeeIdNumber;
    private EntityStatus status;
    private Long hemisId;
    private Long departmentId;
    private String departmentName;
    private Long facultyId;
    private String facultyName;
    private String staffPositionName;
    private String staffPositionCode;
    private String employeeTypeName;
    private String academicRankName;
    private String academicDegreeName;
}
