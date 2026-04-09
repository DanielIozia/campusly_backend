package com.campusly.campusly_backend.auth.service;

import com.campusly.campusly_backend.auth.dto.*;
import com.campusly.campusly_backend.auth.entity.AuthProvider;
import com.campusly.campusly_backend.auth.entity.Role;
import com.campusly.campusly_backend.auth.entity.User;
import com.campusly.campusly_backend.auth.repository.UserRepository;
import com.campusly.campusly_backend.shared.exception.ExceptionBackend;
import com.campusly.campusly_backend.shared.security.JwtService;
import com.campusly.campusly_backend.shared.security.TokenBlacklistService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int ETA_MINIMA = 16;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    // ---------------------------------------------------------------
    // Registrazione utente standard (CAMPUSLY_USER)
    // ---------------------------------------------------------------

    @Transactional
    public UserAuthResponse registerUser(RegisterUserRequest request, HttpServletResponse response) {
        log.info("Registrazione CAMPUSLY_USER: {}", request.email());
        LocalDate birthDate = request.birthDate().toLocalDate();
        validaRegistrazione(request.email(), request.username(), birthDate);

        User user = buildUser(
                request.firstName(), request.lastName(), request.username(),
                request.email(), request.password(), birthDate,
                request.phone(), Role.CAMPUSLY_USER);

        userRepository.save(user);
        addTokenCookie(user, response);
        log.info("CAMPUSLY_USER registrato con successo: {}", user.getEmail());
        return toAuthResponse(user);
    }

    // ---------------------------------------------------------------
    // Registrazione creator (CAMPUSLY_CREATOR)
    // ---------------------------------------------------------------

    @Transactional
    public UserAuthResponse registerCreator(RegisterCreatorRequest request, HttpServletResponse response) {
        log.info("Registrazione CAMPUSLY_CREATOR: {}", request.email());
        LocalDate birthDate = request.birthDate().toLocalDate();
        validaRegistrazione(request.email(), request.username(), birthDate);

        User user = buildUser(
                request.firstName(), request.lastName(), request.username(),
                request.email(), request.password(), birthDate,
                request.phone(), Role.CAMPUSLY_CREATOR);

        userRepository.save(user);
        addTokenCookie(user, response);
        log.info("CAMPUSLY_CREATOR registrato con successo: {}", user.getEmail());
        return toAuthResponse(user);
    }

    // ---------------------------------------------------------------
    // Login (comune a tutti i ruoli)
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public UserAuthResponse login(LoginRequest request, HttpServletResponse response) {
        log.info("Tentativo di login: {}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> ExceptionBackend.fromError(
                        "Credenziali non valide",
                        "Nessun account trovato con questa email. CODICE: AU200",
                        null, HttpStatus.UNAUTHORIZED));

        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw ExceptionBackend.fromError(
                    "Provider non corretto",
                    "Questo account è stato creato con " + user.getAuthProvider().name().toLowerCase()
                            + ". CODICE: AU201",
                    null, HttpStatus.UNAUTHORIZED);
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ExceptionBackend.fromError(
                    "Credenziali non valide",
                    "Password errata. CODICE: AU202",
                    null, HttpStatus.UNAUTHORIZED);
        }

        addTokenCookie(user, response);
        log.info("Login effettuato: {} [{}]", user.getEmail(), user.getRole());
        return toAuthResponse(user);
    }

    // ---------------------------------------------------------------
    // Profilo utente autenticato
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public UserProfileResponse getMe() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ExceptionBackend.fromError(
                        "Utente non trovato",
                        "Impossibile trovare l'utente autenticato. CODICE: AU300",
                        null, HttpStatus.NOT_FOUND));

        return toProfileResponse(user);
    }

    // ---------------------------------------------------------------
    // Logout
    // ---------------------------------------------------------------

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String token = jwtService.getTokenFromCookie(request);
        if (token != null) {
            tokenBlacklistService.blacklist(token);
        }

        Cookie cookie = new Cookie("token", "");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setSecure(jwtService.isSecure());
        cookie.setAttribute("SameSite", jwtService.getSameSiteAttribute());
        response.addCookie(cookie);

        log.info("Logout eseguito, token invalidato");
    }

    // ==================== Private ====================

    private void validaRegistrazione(String email, String username, LocalDate birthDate) {
        if (userRepository.existsByEmail(email)) {
            throw ExceptionBackend.fromError(
                    "Email già registrata",
                    "Esiste già un account con questa email. CODICE: AU100",
                    null, HttpStatus.CONFLICT);
        }

        if (userRepository.existsByUsername(username)) {
            throw ExceptionBackend.fromError(
                    "Username non disponibile",
                    "Questo username è già in uso. CODICE: AU102",
                    null, HttpStatus.CONFLICT);
        }

        int eta = Period.between(birthDate, LocalDate.now()).getYears();
        if (eta < ETA_MINIMA) {
            throw ExceptionBackend.fromError(
                    "Età non consentita",
                    "Devi avere almeno " + ETA_MINIMA + " anni per registrarti. CODICE: AU101",
                    null, HttpStatus.BAD_REQUEST);
        }
    }

    private User buildUser(String firstName, String lastName, String username,
            String email, String password, LocalDate birthDate,
            String phone, Role role) {
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .birthDate(birthDate)
                .phone(phone)
                .authProvider(AuthProvider.LOCAL)
                .role(role)
                .build();
    }

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

    private UserAuthResponse toAuthResponse(User user) {
        return new UserAuthResponse(
                user.getId(), user.getUsername(),
                user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getRole());
    }

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(), user.getUsername(),
                user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getBirthDate(),
                user.getPhone(), user.getPhotoUrl(),
                user.getBio(), user.getRole(),
                user.getAuthProvider(), user.getCreatedAt());
    }
}
