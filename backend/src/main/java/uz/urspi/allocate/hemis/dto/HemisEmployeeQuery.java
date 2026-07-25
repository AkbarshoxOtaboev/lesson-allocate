package uz.urspi.allocate.hemis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HemisEmployeeQuery {

    private Integer page = 1;
    private Integer limit = 50;
    /** teacher | employee | all */
    private String type = "teacher";
    /** HEMIS _department */
    private Long department;
    /** HEMIS _gender */
    private String gender;
    /** HEMIS _staff_position */
    private String staffPosition;
    /** HEMIS _employee_status */
    private String employeeStatus;
    /** HEMIS _employment_form */
    private String employmentForm;
    /** HEMIS _employment_staff */
    private String employmentStaff;
    /** HEMIS _employee_type */
    private String employeeType;
    /** HEMIS _academic_rank */
    private String academicRank;
    /** HEMIS _academic_degree */
    private String academicDegree;
    private String passportPin;
    private String passportNumber;
    private String search;
    private boolean fetchAllPages;
}
