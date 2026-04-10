package com.campusly.campusly_backend.actors.user.services;

import com.campusly.campusly_backend.shared.exception.ExceptionBackend;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.campusly.campusly_backend.shared.security.JwtService;
import com.campusly.campusly_backend.shared.security.TokenBlacklistService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;

import com.campusly.campusly_backend.actors.user.interfaces.auth.LoginRequest;
import com.campusly.campusly_backend.actors.user.interfaces.auth.LoginResponse;
import com.campusly.campusly_backend.actors.user.interfaces.registration.enums.UserStatus;
import com.campusly.campusly_backend.auth.entity.AuthProvider;
import com.campusly.campusly_backend.database.entity.User;
import com.campusly.campusly_backend.database.repository.UserRepository;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    // ---------------------------------------------------------------
    // Login
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        String errorTitle = "Errore login";
        request.isValid(errorTitle);
        String email = normalizeEmail(request.getEmail());

        User user = findUserOrThrow(email, errorTitle);

        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Questo account è stato creato con " + user.getAuthProvider().name().toLowerCase()
                            + ". CODICE: AU202",
                    null, HttpStatus.UNAUTHORIZED);
        }

        // Utente non ha completato la registrazione
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "La registrazione non è stata completata. CODICE: AU203",
                    null, HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Password errata. CODICE: AU204",
                    null, HttpStatus.UNAUTHORIZED);
        }

        addTokenCookie(user, response);
        return LoginResponse.fromUser(user);
    }

    // ---------------------------------------------------------------
    // Profilo utente autenticato (me)
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public void getMe() {
        String errorTitle = "Errore recupero profilo";
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!userRepository.existsByEmail(email)) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Impossibile trovare l'utente autenticato. CODICE: AU300",
                    null, HttpStatus.NOT_FOUND);
        }
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

    // ======================================== 
    //            Metodi privati
    // ========================================

    // Recupera l'utente per email o lancia un'eccezione NOT_FOUND uniforme.
    private User findUserOrThrow(String email, String errorTitle) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> ExceptionBackend.fromError(
                        errorTitle,
                        "Nessun account trovato con questa email. CODICE: AU200",
                        null, HttpStatus.NOT_FOUND));
    }

    // Normalizza l'email: trim e lowercase.
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    // Genera JWT, lo salva in un cookie HttpOnly e lo invia nella response.
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

}
