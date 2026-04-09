package com.campusly.campusly_backend.auth.dto;

import com.campusly.campusly_backend.auth.entity.Role;

import java.util.UUID;

/**
 * DTO di risposta restituito dopo login o registrazione.
 */
public record UserAuthResponse(
        UUID id,
        String username,
        String firstName,
        String lastName,
        String email,
        Role role
) {}
