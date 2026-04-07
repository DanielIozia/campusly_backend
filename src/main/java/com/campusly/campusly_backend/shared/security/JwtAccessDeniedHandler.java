package com.campusly.campusly_backend.shared.security;

import com.campusly.campusly_backend.shared.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handler per gestire le richieste autenticate ma senza i permessi necessari (HTTP 403).
 * <p>
 * Viene invocato da Spring Security quando un utente autenticato tenta di accedere
 * a una risorsa per cui non possiede il ruolo/autorizzazione richiesta.
 * Restituisce un payload JSON coerente con il formato {@link ErrorResponse}.
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        log.warn("Accesso negato: {} {} — utente non ha i permessi necessari",
                request.getMethod(), request.getRequestURI());

        ErrorResponse error = ErrorResponse.of(
                request.getMethod(),
                HttpStatus.FORBIDDEN.value(),
                "Accesso Negato",
                "Non si dispone dei permessi necessari per accedere a questa risorsa."
        );

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        MAPPER.writeValue(response.getOutputStream(), error);
    }
}
