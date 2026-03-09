package com.spunit.performance.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * CDN Integration Configuration for serving static content
 */
@Slf4j
@Getter
@Configuration
public class CdnConfig implements WebMvcConfigurer {

    @Value("${cdn.enabled:true}")
    private boolean enabled;

    @Value("${cdn.base-url:https://cdn.example.com}")
    private String baseUrl;

    @Value("${cdn.cache-control:public, max-age=31536000}")
    private String cacheControl;

    @Value("#{'${cdn.static-resources:/images/**,/css/**,/js/**}'.split(',')}")
    private List<String> staticResources;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (enabled) {
            // Configure static resource handlers with caching
            registry.addResourceHandler("/static/**")
                    .addResourceLocations("classpath:/static/")
                    .setCachePeriod(31536000);

            registry.addResourceHandler("/images/**")
                    .addResourceLocations("classpath:/static/images/")
                    .setCachePeriod(31536000);

            registry.addResourceHandler("/css/**")
                    .addResourceLocations("classpath:/static/css/")
                    .setCachePeriod(31536000);

            registry.addResourceHandler("/js/**")
                    .addResourceLocations("classpath:/static/js/")
                    .setCachePeriod(31536000);

            log.info("CDN integration enabled with base URL: {}", baseUrl);
        }
    }

    /**
     * Get CDN URL for a static resource
     */
    public String getCdnUrl(String resourcePath) {
        if (enabled && resourcePath != null) {
            return baseUrl + resourcePath;
        }
        return resourcePath;
    }

    /**
     * Check if path should be served via CDN
     */
    public boolean shouldUseCdn(String path) {
        if (!enabled || path == null) {
            return false;
        }
        return staticResources.stream().anyMatch(pattern ->
            matchesPattern(path, pattern));
    }

    private boolean matchesPattern(String path, String pattern) {
        String regex = pattern.replace("**", ".*").replace("*", "[^/]*");
        return path.matches(regex);
    }
}
