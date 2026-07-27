package uz.urspi.allocate.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Vue Router (history mode) uchun: mavjud bo'lmagan yo'llarni index.html ga qaytaradi.
 * /api, swagger va boshqa backend yo'llariga tegmaydi.
 */
@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        if (isBackendPath(resourcePath)) {
                            return null;
                        }
                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }
                        return new ClassPathResource("/static/index.html");
                    }
                });
    }

    private static boolean isBackendPath(String path) {
        return path.startsWith("api/")
                || path.startsWith("v3/")
                || path.startsWith("swagger-ui")
                || path.startsWith("h2-console")
                || path.startsWith("uploads/")
                || path.startsWith("actuator/");
    }
}
