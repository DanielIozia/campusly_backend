package com.campusly.campusly_backend.shared.exception;

public record ErrorDetail(
        String title,
        String message,
        Object payload
) {}
