package com.campusly.campusly_backend.shared.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servizio per la blacklist dei token JWT invalidati (logout).
 * <p>
 * Mantiene in memoria i token revocati fino alla loro scadenza naturale,
 * dopodiché vengono rimossi automaticamente da un job schedulato.
 * <p>
 * <strong>Nota:</strong> In un setup di produzione con più istanze,
 * sostituire con Redis o un altro store distribuito.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final JwtService jwtService;

    /**
     * Mappa token → data di scadenza. Usata per cleanup periodico.
     */
    private final Map<String, Date> blacklistedTokens = new ConcurrentHashMap<>();

    /**
     * Aggiunge un token alla blacklist. Il token resta in blacklist
     * fino alla sua scadenza naturale.
     */
    public void blacklist(String token) {
        try {
            Claims claims = jwtService.extractClaims(token);
            blacklistedTokens.put(token, claims.getExpiration());
            log.info("Token aggiunto alla blacklist (scade: {})", claims.getExpiration());
        } catch (Exception e) {
            // Se il token è già scaduto o invalido, non serve aggiungerlo
            log.debug("Token non aggiunto alla blacklist (già invalido): {}", e.getMessage());
        }
    }

    /**
     * Verifica se un token è nella blacklist.
     */
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    /**
     * Rimuove i token scaduti dalla blacklist ogni 15 minuti.
     */
    @Scheduled(fixedRate = 900_000)
    public void cleanupExpiredTokens() {
        Date now = new Date();
        int before = blacklistedTokens.size();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));
        int removed = before - blacklistedTokens.size();
        if (removed > 0) {
            log.debug("Cleanup blacklist: rimossi {} token scaduti, {} rimanenti",
                    removed, blacklistedTokens.size());
        }
    }
}
