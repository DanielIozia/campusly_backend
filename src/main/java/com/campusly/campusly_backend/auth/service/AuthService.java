package com.campusly.campusly_backend.auth.service;

import com.campusly.campusly_backend.auth.dto.*;
import com.campusly.campusly_backend.auth.entity.AuthProvider;
import com.campusly.campusly_backend.auth.entity.User;
import com.campusly.campusly_backend.auth.repository.UserRepository;
import com.campusly.campusly_backend.shared.exception.ExceptionBackend;
import com.campusly.campusly_backend.shared.security.JwtService;
import com.campusly.campusly_backend.shared.security.TokenBlacklistService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Registra un nuovo utente con autenticazione locale (email/password).
     */
    @Transactional
    public UserAuthResponse register(RegisterRequest request, HttpServletResponse response) {
        final String errorTitle = "Errore registrazione utente";

        if (userRepository.existsByEmail(request.email())) {
            throw ExceptionBackend.fromError(
                    errorTitle, "Email già registrata, CODICE: AU100", request,
                    HttpStatus.CONFLICT
            );
        }

        LocalDate dateOfBirth = toLocalDate(request.dateOfBirth());
        String username = generateUsername(request.name(), request.surname());

        User user = User.builder()
                .username(username)
                .name(request.name().trim())
                .surname(request.surname().trim())
                .email(request.email().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .dateOfBirth(dateOfBirth)
                .telephone(request.telephone())
                .authProvider(AuthProvider.LOCAL)
                .build();

        user = userRepository.save(user);
        log.info("Nuovo utente registrato: {} {} ({})", user.getName(), user.getSurname(), user.getEmail());

        addTokenCookie(user, response);
        return new UserAuthResponse(user.getId(), user.getName(), user.getEmail());
    }

    /**
     * Autentica un utente con email e password.
     */
    @Transactional(readOnly = true)
    public UserAuthResponse login(LoginRequest request, HttpServletResponse response) {
        final String errorTitle = "Errore login";

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> ExceptionBackend.fromError(
                        errorTitle, "Credenziali non valide, CODICE: AU200", request,
                        HttpStatus.UNAUTHORIZED
                ));

        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Questo account utilizza il login con " + user.getAuthProvider()
                            + ". Usa il provider corretto per accedere, CODICE: AU201",
                    request, HttpStatus.BAD_REQUEST
            );
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ExceptionBackend.fromError(
                    errorTitle, "Credenziali non valide, CODICE: AU202", request,
                    HttpStatus.UNAUTHORIZED
            );
        }

        log.info("Login riuscito per utente: {}", user.getEmail());
        addTokenCookie(user, response);
        return new UserAuthResponse(user.getId(), user.getName(), user.getEmail());
    }

    /**
     * Restituisce il profilo dell'utente autenticato a partire dall'email
     * estratta dal JWT (impostata nel SecurityContext dal filtro).
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getAuthenticatedUser(UUID authenticatedUserId) {
        final String errorTitle = "Errore recupero profilo";

        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> ExceptionBackend.fromError(
                        errorTitle, "Utente non trovato, CODICE: AU300", authenticatedUserId,
                        HttpStatus.UNAUTHORIZED
                ));

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getDateOfBirth(),
                user.getTelephone(),
                user.getPhotoUrl(),
                user.getBio(),
                user.getRole().name(),
                user.getAuthProvider().name(),
                user.getCreatedAt()
        );
    }

    /**
     * Invalida l'access token corrente aggiungendolo alla blacklist.
     */
    public void logout(String accessToken) {
        tokenBlacklistService.blacklist(accessToken);
        log.info("Logout eseguito, token invalidato");
    }

    // ==================== Private ====================

    private void addTokenCookie(User user, HttpServletResponse response) {
        String token = jwtService.generateToken(
                user.getId(), user.getEmail(), user.getRole().name());

        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(jwtService.getTokenMaxAgeSeconds());
        cookie.setSecure(jwtService.isSecure());
        cookie.setAttribute("SameSite", jwtService.getSameSiteAttribute());
        response.addCookie(cookie);
    }

    /**
     * Converte il DTO DateOfBirth in LocalDate, validando la data.
     */
    private LocalDate toLocalDate(RegisterRequest.DateOfBirth dob) {
        try {
            return LocalDate.of(dob.year(), dob.month(), dob.day());
        } catch (Exception e) {
            throw ExceptionBackend.fromError(
                    "Errore registrazione utente",
                    "Data di nascita non valida: " + dob.day() + "/" + dob.month() + "/" + dob.year() + ", CODICE: AU101",
                    null, HttpStatus.BAD_REQUEST
            );
        }
    }

    /**
     * Genera un username univoco a partire da nome e cognome.
     * Formato: nome.cognome (lowercase, senza spazi). Se esiste già, aggiunge un suffisso random.
     */
    private String generateUsername(String name, String surname) {
        String base = (name.trim() + "." + surname.trim())
                .toLowerCase()
                .replaceAll("\\s+", "");

        if (base.length() > 45) {
            base = base.substring(0, 45);
        }

        if (!userRepository.existsByUsername(base)) {
            return base;
        }

        // Aggiunge suffisso numerico random
        String candidate;
        do {
            String suffix = UUID.randomUUID().toString().substring(0, 4);
            candidate = base.length() > 44 ? base.substring(0, 44) + "." + suffix : base + "." + suffix;
        } while (userRepository.existsByUsername(candidate));

        return candidate;
    }
}
