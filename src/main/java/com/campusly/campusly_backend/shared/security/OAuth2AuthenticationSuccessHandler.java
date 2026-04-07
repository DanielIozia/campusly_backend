package com.campusly.campusly_backend.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Handler invocato dopo un login OAuth2 (Google) avvenuto con successo.
 * <p>
 * Genera un JWT (access + refresh token) a partire dalle informazioni
 * dell'utente Google e li restituisce come JSON nella risposta.
 * <p>
 * <strong>Nota:</strong> In un setup completo, qui si dovrebbe cercare/creare
 * l'utente nel database e usare l'ID reale. Per ora, genera un UUID deterministico
 * basato sull'email per mantenere coerenza tra le sessioni.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final JwtService jwtService;

    @Value("${app.security.oauth2.frontend-redirect-url:}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // In un setup completo, qui si cerca/crea l'utente nel DB
        // Per ora usiamo un UUID deterministico basato sull'email
        UUID userId = UUID.nameUUIDFromBytes(email.getBytes());
        String role = "STUDENT";

        String accessToken = jwtService.generateAccessToken(userId, email, role);
        String refreshToken = jwtService.generateRefreshToken(userId, email);

        log.info("Login OAuth2 riuscito per utente: {} ({})", name, email);

        // Se c'è un frontend redirect URL configurato, redirige con il token come query param
        if (frontendRedirectUrl != null && !frontendRedirectUrl.isBlank()) {
            String redirectUrl = frontendRedirectUrl
                    + "?accessToken=" + accessToken
                    + "&refreshToken=" + refreshToken;
            response.sendRedirect(redirectUrl);
            return;
        }

        // Altrimenti restituisce i token come JSON (utile per test)
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        MAPPER.writeValue(response.getOutputStream(), Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "email", email
        ));
    }
}
