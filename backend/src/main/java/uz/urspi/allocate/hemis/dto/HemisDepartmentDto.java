package uz.urspi.allocate.hemis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class HemisDepartmentDto {

    private Long id;
    private String name;
    private String code;
    private Long parent;
    private Boolean active;
    private Classifier structureType;
    private Classifier localityType;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Classifier {
        private String code;
        private String name;
    }
}
