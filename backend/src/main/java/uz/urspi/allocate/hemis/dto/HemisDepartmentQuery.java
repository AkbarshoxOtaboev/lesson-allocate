package uz.urspi.allocate.hemis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HemisDepartmentQuery {

    private Integer page = 1;
    private Integer limit = 50;
    /** 0 | 1 | all */
    private String active = "1";
    /** HEMIS query param _structure_type */
    private String structureType;
    private Long parent;
    /** true bo'lsa barcha sahifalarni oqib sync qiladi */
    private boolean fetchAllPages;
}
