package uz.urspi.allocate.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uz.urspi.allocate.security.JwtProperties;

@Configuration
@EnableConfigurationProperties({
        JwtProperties.class,
        CorsProperties.class,
        StorageProperties.class,
        HemisProperties.class
})
public class AppProperties {
}
