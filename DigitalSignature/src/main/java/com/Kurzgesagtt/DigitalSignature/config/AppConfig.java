package com.Kurzgesagtt.DigitalSignature.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig {
    @Bean
    public WebMvcConfigurer corsConfig(){
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowCredentials(true);
            }
        };
    }

    @Bean
    public MultipartConfigElement multipartConfigElement(){
        MultipartConfigFactory configFactory = new MultipartConfigFactory();
        configFactory.setMaxFileSize(DataSize.parse("10MB"));
        configFactory.setMaxRequestSize(DataSize.parse("10MB"));
        return configFactory.createMultipartConfig();
    }

}
