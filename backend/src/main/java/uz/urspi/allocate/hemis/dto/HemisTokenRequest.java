package uz.urspi.allocate.hemis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HemisTokenRequest {

    @NotBlank
    private String accessToken;

    private String baseUrl;

    private String description;
}
