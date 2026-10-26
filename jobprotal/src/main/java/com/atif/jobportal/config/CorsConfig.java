
package com.atif.jobportal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    private final String frontendUrl;

    public CorsConfig(
            @Value("${app.frontend-url:${FRONTEND_URL:http://localhost:5500}}")
            String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(
                                "http://localhost:5500",
                                "http://127.0.0.1:5500",
                                "http://localhost:5501",
                                "http://127.0.0.1:5501",
                                frontendUrl
                        )
                        .allowedMethods(
                                "GET", "POST", "PUT", "DELETE", "OPTIONS"
                        )
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
