package com.campusly.campusly_backend.shared.config;

import org.springframework.context.annotation.Configuration;

/**
 * CORS configuration moved to SecurityConfig.corsConfigurationSource()
 * to ensure preflight requests are handled correctly by Spring Security.
 */
@Configuration
public class CorsConfig {
}