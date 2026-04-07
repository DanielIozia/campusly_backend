package com.campusly.campusly_backend.health.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckResponse {

    private String status;
    private String message;
    private Instant timestamp;
}
