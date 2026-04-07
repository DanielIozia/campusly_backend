package com.campusly.campusly_backend.health.service;

import com.campusly.campusly_backend.health.dto.HealthCheckResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class HealthCheckService {

    public HealthCheckResponse getHealthStatus() {
        return HealthCheckResponse.builder()
                .status("UP")
                .message("Campusly Backend is running")
                .timestamp(Instant.now())
                .build();
    }
}
