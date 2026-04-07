package com.campusly.campusly_backend.auth.controller;

import com.campusly.campusly_backend.auth.dto.*;
import com.campusly.campusly_backend.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register — Registrazione nuovo utente (email/password).
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/login — Login con email e password.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/auth/me — Restituisce il profilo dell'utente autenticato.
     * Richiede un JWT valido nell'header Authorization.
     * Se il token manca o è invalido, Spring Security restituisce 401 automaticamente.
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(Authentication authentication) {
        String email = authentication.getName();
        UserProfileResponse profile = authService.getAuthenticatedUser(email);
        return ResponseEntity.ok(profile);
    }

    /**
     * POST /api/auth/logout — Invalida l'access token corrente.
     * Richiede un JWT valido nell'header Authorization.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring("Bearer ".length());
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }
}
