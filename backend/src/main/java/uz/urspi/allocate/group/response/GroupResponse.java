package uz.urspi.allocate.group.response;

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
public class GroupResponse {

    private Long id;
    private String name;
    private EntityStatus status;
    private Long departmentId;
    private String departmentName;
    private Long facultyId;
    private String facultyName;
    private String hemisDepartmentName;
    private Long hemisId;
    private Boolean hemisActive;
    private Long curriculumHemisId;
    private String curriculumName;
    private Long specialtyHemisId;
    private String specialtyName;
    private String educationTypeCode;
    private String educationTypeName;
    private String educationFormCode;
    private String educationFormName;
    private String educationLangCode;
    private String educationLangName;
}
