package top.keiskeiframework.common.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootConfiguration
public class MyWebConfigurer implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("POST", "GET", "PUT", "OPTIONS", "DELETE", "PATCH")
                .maxAge(3600)
                .allowedHeaders("*")
                .allowCredentials(true);
    }

}
