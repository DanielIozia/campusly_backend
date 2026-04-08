package com.campusly.campusly_backend.auth.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Risposta restituita dall'endpoint {@code GET /api/auth/me}.
 * Contiene le informazioni profilo dell'utente autenticato.
 */
public record UserProfileResponse(
        UUID id,
        String username,
        String name,
        String surname,
        String email,
        java.time.LocalDate dateOfBirth,
        String telephone,
        String photoUrl,
        String bio,
        String role,
        String authProvider,
        Instant createdAt) {
}
