package com.mykr.acgnhistoryanalyzer.config;

import com.mykr.acgnhistoryanalyzer.service.CoverStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final CoverStorageService coverStorageService;

    public StaticResourceConfig(CoverStorageService coverStorageService) {
        this.coverStorageService = coverStorageService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(CoverStorageService.COVER_ASSET_PREFIX + "**")
                .addResourceLocations(coverStorageService.getCoverRoot().toUri().toString());
    }
}
