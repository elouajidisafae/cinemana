package com.example.cinimana.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configuration pour servir les fichiers uploadés
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
