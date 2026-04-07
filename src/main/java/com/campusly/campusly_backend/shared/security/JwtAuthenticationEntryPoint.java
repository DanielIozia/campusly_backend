package com.campusly.campusly_backend.shared.security;

import com.campusly.campusly_backend.shared.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Entry point per gestire le richieste non autenticate (HTTP 401).
 * <p>
 * Viene invocato da Spring Security quando una richiesta protetta arriva
 * senza un token JWT valido. Restituisce un payload JSON coerente
 * con il formato {@link ErrorResponse} usato dal GlobalExceptionHandler.
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.warn("Accesso non autorizzato: {} {}", request.getMethod(), request.getRequestURI());

        ErrorResponse error = ErrorResponse.of(
                request.getMethod(),
                HttpStatus.UNAUTHORIZED.value(),
                "Non Autorizzato",
                "Autenticazione richiesta. Fornire un token JWT valido nell'header Authorization."
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        MAPPER.writeValue(response.getOutputStream(), error);
    }
}
