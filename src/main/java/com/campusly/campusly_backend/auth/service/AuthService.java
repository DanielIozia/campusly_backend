package com.campusly.campusly_backend.auth.service;

import com.campusly.campusly_backend.auth.dto.*;
import com.campusly.campusly_backend.auth.entity.AuthProvider;
import com.campusly.campusly_backend.auth.entity.User;
import com.campusly.campusly_backend.auth.repository.UserRepository;
import com.campusly.campusly_backend.shared.exception.BadRequestException;
import com.campusly.campusly_backend.shared.exception.ResourceNotFoundException;
import com.campusly.campusly_backend.shared.security.JwtService;
import com.campusly.campusly_backend.shared.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email già registrata");
        }

        LocalDate dataNascita = toLocalDate(request.dataNascita());
        String username = generateUsername(request.nome(), request.cognome());

        User user = User.builder()
                .username(username)
                .nome(request.nome().trim())
                .cognome(request.cognome().trim())
                .email(request.email().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .dataNascita(dataNascita)
                .telefono(request.telefono())
                .authProvider(AuthProvider.LOCAL)
                .build();

        user = userRepository.save(user);
        log.info("Nuovo utente registrato: {} {} ({})", user.getNome(), user.getCognome(), user.getEmail());

        return generateTokens(user);
    }

    /**
     * Autentica un utente con email e password.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Credenziali non valide"));

        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new BadRequestException(
                    "Questo account utilizza il login con " + user.getAuthProvider()
                            + ". Usa il provider corretto per accedere.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Credenziali non valide");
        }

        log.info("Login riuscito per utente: {}", user.getEmail());
        return generateTokens(user);
    }

    /**
     * Restituisce il profilo dell'utente autenticato a partire dall'email
     * estratta dal JWT (impostata nel SecurityContext dal filtro).
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getAuthenticatedUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", "email", email));

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getNome(),
                user.getCognome(),
                user.getEmail(),
                user.getDataNascita(),
                user.getTelefono(),
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

    private AuthResponse generateTokens(User user) {
        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(
                user.getId(), user.getEmail());
        return new AuthResponse(accessToken, refreshToken);
    }

    /**
     * Converte il DTO DateOfBirth in LocalDate, validando la data.
     */
    private LocalDate toLocalDate(RegisterRequest.DateOfBirth dob) {
        try {
            return LocalDate.of(dob.anno(), dob.mese(), dob.giorno());
        } catch (Exception e) {
            throw new BadRequestException("Data di nascita non valida: " + dob.giorno() + "/" + dob.mese() + "/" + dob.anno());
        }
    }

    /**
     * Genera un username univoco a partire da nome e cognome.
     * Formato: nome.cognome (lowercase, senza spazi). Se esiste già, aggiunge un suffisso random.
     */
    private String generateUsername(String nome, String cognome) {
        String base = (nome.trim() + "." + cognome.trim())
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
