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

//INTERFACES
import com.campusly.campusly_backend.actors.user.interfaces.auth.LoginRequest;
import com.campusly.campusly_backend.actors.user.interfaces.auth.LoginResponse;
import com.campusly.campusly_backend.actors.user.interfaces.registration.RegistrationRequest;
//ENTITY
import com.campusly.campusly_backend.auth.entity.AuthProvider;
import com.campusly.campusly_backend.auth.entity.Role;
import com.campusly.campusly_backend.auth.entity.User;

//REPOSITORY
import com.campusly.campusly_backend.auth.repository.UserRepository;

import java.time.LocalDate;
import java.time.Period;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MIN_AGE = 16;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    // ---------------------------------------------------------------
    // Registrazione utente standard (CAMPUSLY_USER)
    // ---------------------------------------------------------------

    @Transactional
    public LoginResponse registerUser(RegistrationRequest request, HttpServletResponse response) {
        String errorTitle = "Errore registrazione";
        request.getBirthDate().isValid();
        request.isValid(errorTitle);

        LocalDate birthDate = request.getBirthDate().toLocalDate();
        validaRegistrazione(request.getEmail(), request.getUsername(), birthDate);

        User user = buildUser(
                request.getFirstName(), request.getLastName(), request.getUsername(),
                request.getEmail(), request.getPassword(), birthDate,
                request.getPhone(), Role.CAMPUSLY_USER);

        userRepository.save(user);
        addTokenCookie(user, response);
        return LoginResponse.fromUser(user);
    }

    // ---------------------------------------------------------------
    // Login CAMPUSLY_USER
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        String errorTitle = "Errore login";
        request.isValid(errorTitle);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> ExceptionBackend.fromError(
                        errorTitle,
                        "Nessun account trovato. CODICE: AU201",
                        null, HttpStatus.UNAUTHORIZED));

        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Questo account è stato creato con " + user.getAuthProvider().name().toLowerCase()
                            + ". CODICE: AU202",
                    null, HttpStatus.UNAUTHORIZED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Password errata. CODICE: AU203",
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
        String erroriTitle = "Errore recupero profilo";
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!userRepository.existsByEmail(email)) {
            throw ExceptionBackend.fromError(
                    erroriTitle,
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
        if (eta < MIN_AGE) {
            throw ExceptionBackend.fromError(
                    "Età non consentita",
                    "Devi avere almeno " + MIN_AGE + " anni per registrarti. CODICE: AU101",
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
}
