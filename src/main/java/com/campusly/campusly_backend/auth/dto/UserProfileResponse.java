package com.campusly.campusly_backend.auth.dto;

import com.campusly.campusly_backend.auth.entity.AuthProvider;
import com.campusly.campusly_backend.auth.entity.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO di risposta per il profilo utente autenticato (GET /api/auth/me).
 */
public record UserProfileResponse(
        UUID id,
        String username,
        String firstName,
        String lastName,
        String email,
        LocalDate birthDate,
        String phone,
        String photoUrl,
        String bio,
        Role role,
        AuthProvider authProvider,
        LocalDateTime createdAt
) {}
