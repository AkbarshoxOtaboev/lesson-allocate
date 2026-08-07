package uz.urspi.allocate.hemis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HemisSpecialtyQuery {

    private Integer page = 1;
    private Integer limit = 50;
    /** HEMIS query param _department */
    private Long department;
    /** HEMIS query param _locality_type */
    private String localityType;
    /** HEMIS query param _education_type */
    private String educationType;
    private boolean fetchAllPages;
}
