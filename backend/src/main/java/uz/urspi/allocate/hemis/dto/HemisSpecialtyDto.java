package uz.urspi.allocate.hemis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class HemisSpecialtyDto {

    private Long id;
    private String name;
    private String code;
    private Boolean active;
    private DepartmentRef department;
    private Classifier educationType;
    private Classifier localityType;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DepartmentRef {
        private Long id;
        private String name;
        private String code;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Classifier {
        private String code;
        private String name;
    }
}
