package uz.urspi.allocate.hemis.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class HemisTokenResponse {

    private Long id;
    private String provider;
    private String maskedToken;
    private boolean configured;
    private String baseUrl;
    private String description;
    private LocalDateTime updatedAt;
}
