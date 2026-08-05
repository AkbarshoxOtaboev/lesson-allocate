package uz.urspi.allocate.hemis.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class HemisGroupDto {

    private Long id;
    private String name;
    private Boolean active;
    private DepartmentRef department;
    private Classifier educationLang;

    @JsonProperty("_curriculum")
    private Long curriculumId;

    /** HEMIS API uses misspelled field name "specilaty". */
    @JsonAlias("specilaty")
    private Ref specialty;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Ref {
        private Long id;
        private String name;
        private String code;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DepartmentRef {
        private Long id;
        private String name;
        private String code;
        private Long parent;
        private Boolean active;
        private Classifier structureType;
        private Classifier localityType;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Classifier {
        private String code;
        private String name;
    }
}
