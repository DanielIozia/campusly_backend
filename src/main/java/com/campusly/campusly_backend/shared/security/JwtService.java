package com.campusly.campusly_backend.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Servizio per la generazione e la validazione dei token JWT.
 * Utilizza la libreria jjwt (io.jsonwebtoken) per firmare e verificare i token.
 */
@Slf4j
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${app.security.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    /**
     * Genera un access token JWT con le claim standard.
     *
     * @param userId   ID dell'utente (UUID)
     * @param email    email dell'utente
     * @param role     ruolo dell'utente (es. STUDENT)
     * @return il token JWT firmato
     */
    public String generateAccessToken(UUID userId, String email, String role) {
        return buildToken(userId, email, Map.of("role", role), accessTokenExpirationMs);
    }

    /**
     * Genera un refresh token JWT (claim minimali).
     *
     * @param userId ID dell'utente (UUID)
     * @param email  email dell'utente
     * @return il refresh token JWT firmato
     */
    public String generateRefreshToken(UUID userId, String email) {
        return buildToken(userId, email, Map.of("type", "refresh"), refreshTokenExpirationMs);
    }

    /**
     * Estrae tutte le claim dal token JWT.
     * Lancia {@link JwtException} se il token è invalido o scaduto.
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Estrae il subject (email) dal token.
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Estrae l'userId dalla claim "userId".
     */
    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaims(token).get("userId", String.class));
    }

    /**
     * Estrae il ruolo dalla claim "role".
     */
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    /**
     * Verifica se il token è valido (firma corretta e non scaduto).
     */
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token JWT non valido: {}", e.getMessage());
            return false;
        }
    }

    // ==================== Private ====================

    private String buildToken(UUID userId, String email, Map<String, Object> extraClaims,
                              long expirationMs) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .claims(extraClaims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(signingKey)
                .compact();
    }
}
