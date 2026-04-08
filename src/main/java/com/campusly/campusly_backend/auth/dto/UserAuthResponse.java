package com.campusly.campusly_backend.auth.dto;

import java.util.UUID;

public record UserAuthResponse(UUID id, String name, String email) {}
