package uz.urspi.allocate.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.hemis")
public class HemisProperties {

    private String baseUrl = "https://student.urspi.uz/rest";

    private String language = "uz-UZ";

    private int pageSize = 200;
}
