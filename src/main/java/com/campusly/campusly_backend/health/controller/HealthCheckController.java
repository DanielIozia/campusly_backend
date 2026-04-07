package com.campusly.campusly_backend.health.controller;

import com.campusly.campusly_backend.health.dto.HealthCheckResponse;
import com.campusly.campusly_backend.health.service.HealthCheckService;
import com.campusly.campusly_backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthCheckController {

    private final HealthCheckService healthCheckService;

    @GetMapping
    public ResponseEntity<HealthCheckResponse> healthCheck() {
        return ResponseEntity.ok(healthCheckService.getHealthStatus());
    }

    /**
     * Endpoint di test per verificare il GlobalExceptionHandler.
     * Esempio: GET /api/health/test-error/123 → 404 con ErrorResponse JSON.
     */
    @GetMapping("/test-error/{id}")
    public ResponseEntity<Void> testError(@PathVariable Long id) {
        throw new ResourceNotFoundException("HealthCheck", "id", id);
    }
}
