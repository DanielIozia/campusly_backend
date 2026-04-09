package com.campusly.campusly_backend.auth.entity;

/**
 * Ruoli disponibili nella piattaforma Campusly.
 */
public enum Role {

    /** Utente standard: può postare spotted, mettere like, segnalare contenuti. */
    CAMPUSLY_USER,

    /** Creator: può pubblicare eventi sulla piattaforma. Registrazione tramite flusso dedicato. */
    CAMPUSLY_CREATOR,

    /** Moderatore: approva/rimuove post e gestisce le segnalazioni a livello globale. Creato da SUPER_ADMIN. */
    CAMPUSLY_MODERATOR,

    /** Super Admin: gestisce i moderatori e ha accesso completo alla piattaforma. */
    SUPER_ADMIN
}
