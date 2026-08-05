package uz.urspi.allocate.hemis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HemisGroupQuery {

    private Integer page = 1;
    private Integer limit = 50;
    private Long id;
    /** HEMIS query param _department */
    private Long department;
    /** HEMIS query param _curriculum */
    private Long curriculum;
    /** HEMIS query param _specialty */
    private Long specialty;
    /** HEMIS query param _education_type */
    private String educationType;
    /** HEMIS query param _education_form */
    private String educationForm;
    private boolean fetchAllPages;
}
