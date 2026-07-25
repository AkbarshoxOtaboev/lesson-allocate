package uz.urspi.allocate.hemis.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class HemisEmployeeDto {

    private Long id;

    @JsonAlias({"full_name", "fullName"})
    private String fullName;

    @JsonAlias({"short_name", "shortName"})
    private String shortName;

    @JsonAlias({"first_name", "firstname", "firstName"})
    private String firstName;

    @JsonAlias({"second_name", "secondname", "secondName"})
    private String secondName;

    @JsonAlias({"third_name", "thirdname", "thirdName"})
    private String thirdName;

    @JsonAlias({"employee_id_number", "employeeIdNumber"})
    private String employeeIdNumber;

    @JsonAlias({"birth_date", "birthDate"})
    private String birthDate;

    private String image;

    private Classifier gender;

    private DepartmentRef department;

    private Classifier staffPosition;
    private Classifier employeeStatus;
    private Classifier employmentForm;
    private Classifier employmentStaff;
    private Classifier employeeType;
    private Classifier academicRank;
    private Classifier academicDegree;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Classifier {
        private String code;
        private String name;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DepartmentRef {
        private Long id;
        private String name;
        private String code;
    }
}
