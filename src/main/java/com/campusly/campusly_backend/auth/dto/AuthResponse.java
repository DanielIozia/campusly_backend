package com.campusly.campusly_backend.auth.dto;

/**
 * Risposta restituita dopo login o registrazione avvenuta con successo.
 */
public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
