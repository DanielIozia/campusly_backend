package com.campusly.campusly_backend.shared.security;

/**
 * Costanti centralizzate per i ruoli Campusly.
 *
 * Gerarchia:
 *
 *   SUPER_ADMIN
 *       └─► CAMPUSLY_MODERATOR
 *               ├─► CAMPUSLY_USER     ← utente standard
 *               └─► CAMPUSLY_CREATOR  ← stesso livello di USER, può creare eventi
 *
 * USER e CREATOR sono peer: stessi endpoint accessibili.
 * Differenza funzionale: solo CREATOR può creare eventi.
 * MODERATOR e SUPER_ADMIN ereditano entrambi i ruoli.
 */
public final class AppRoles {

    private AppRoles() {}

    // ── Singoli ruoli ────────────────────────────────────────────────────────
    public static final String USER      = "CAMPUSLY_USER";
    public static final String CREATOR   = "CAMPUSLY_CREATOR";
    public static final String MODERATOR = "CAMPUSLY_MODERATOR";
    public static final String ADMIN     = "SUPER_ADMIN";

    // ── Gruppi per le regole di sicurezza ────────────────────────────────────

    /** Utenti standard dell'app: USER, CREATOR, MODERATOR, ADMIN */
    public static final String[] AUTHENTICATED_USERS = {USER, CREATOR, MODERATOR, ADMIN};

    /** Solo chi può creare eventi: CREATOR, MODERATOR, ADMIN */
    public static final String[] EVENT_CREATORS = {CREATOR, MODERATOR, ADMIN};

    /** Moderatori e admin: MODERATOR, ADMIN */
    public static final String[] MODERATORS = {MODERATOR, ADMIN};

    /** Solo Super Admin */
    public static final String[] ADMIN_ONLY = {ADMIN};
}
