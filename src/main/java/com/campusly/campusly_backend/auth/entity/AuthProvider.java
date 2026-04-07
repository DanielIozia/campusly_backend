package com.campusly.campusly_backend.auth.entity;

/**
 * Provider di autenticazione supportati.
 * Mappa il campo {@code auth_provider} della tabella {@code users}.
 */
public enum AuthProvider {
    LOCAL,
    GOOGLE
}
