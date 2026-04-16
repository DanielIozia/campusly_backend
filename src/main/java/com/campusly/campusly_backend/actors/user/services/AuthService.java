package com.campusly.campusly_backend.actors.user.services;

import com.campusly.campusly_backend.database.entity.OtpToken;
import com.campusly.campusly_backend.database.entity.OtpTokenType;
import com.campusly.campusly_backend.database.repository.OtpTokenRepository;
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
import com.campusly.campusly_backend.actors.user.interfaces.auth.ForgotPasswordRequest;
import com.campusly.campusly_backend.actors.user.interfaces.auth.VerifyOtpRequest;
import com.campusly.campusly_backend.actors.user.interfaces.auth.ResetPasswordRequest;
import com.campusly.campusly_backend.actors.user.interfaces.registration.enums.UserStatus;
import com.campusly.campusly_backend.auth.entity.AuthProvider;
import com.campusly.campusly_backend.database.entity.User;
import com.campusly.campusly_backend.database.repository.UserRepository;

import com.campusly.campusly_backend.shared.email.EmailService;
import java.time.LocalDateTime;
import java.util.List;
import java.security.SecureRandom;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final EmailService emailService;

    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

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
    public LoginResponse getMe() {
        String errorTitle = "Errore recupero profilo";
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = findUserOrThrow(email, errorTitle);
        return LoginResponse.fromUser(user);
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

    // ---------------------------------------------------------------
    // forgot password → invio OTP via email
    // ---------------------------------------------------------------

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());
        String errorTitle = "Recupero password";
        User user = findUserOrThrow(email, errorTitle);

        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Questo account è stato creato con " + user.getAuthProvider().name().toLowerCase() + ". CODICE: AU202",
                    null, HttpStatus.UNAUTHORIZED);
        }

        // Solo utenti attivi o già in recovery possono richiedere recovery
        if (user.getStatus() != UserStatus.ACTIVE && user.getStatus() != UserStatus.OTP_PASSWORD_RECOVERY) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Non è possibile recuperare la password in questo stato. CODICE: AU210",
                    null, HttpStatus.BAD_REQUEST);
        }

        // Invalida token precedenti e crea nuovo OTP
        otpTokenRepository.deleteByUserIdAndTokenType(user.getId(), OtpTokenType.PASSWORD_RESET);
        OtpToken token = buildOtpToken(user.getId(), OtpTokenType.PASSWORD_RESET);
        otpTokenRepository.save(token);

        user.setStatus(UserStatus.OTP_PASSWORD_RECOVERY);
        userRepository.save(user);
        sendOtpEmail(email, token.getOtpCode());
    }

    @Transactional
    public void verifyPasswordRecoveryOtp(VerifyOtpRequest request) {
        String email = normalizeEmail(request.getEmail());
        String errorTitle = "Verifica OTP recupero password";
        User user = findUserOrThrow(email, errorTitle);

        if (user.getStatus() != UserStatus.OTP_PASSWORD_RECOVERY) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Nessun codice OTP in attesa di verifica per questa email. CODICE: AU213",
                    null, HttpStatus.BAD_REQUEST);
        }

        OtpToken token = otpTokenRepository
                .findFirstByUserIdAndTokenTypeAndUsedFalseOrderByCreatedAtDesc(
                        user.getId(), OtpTokenType.PASSWORD_RESET)
                .orElseThrow(() -> ExceptionBackend.fromError(
                        errorTitle,
                        "Nessun codice OTP attivo. Richiedi un nuovo codice. CODICE: AU214",
                        null, HttpStatus.GONE));

        if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Il codice OTP è scaduto. Richiedi un nuovo codice. CODICE: AU214",
                    null, HttpStatus.GONE);
        }

        if (!token.getOtpCode().equals(request.getOtpCode().trim())) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Codice OTP non valido. CODICE: AU215",
                    null, HttpStatus.BAD_REQUEST);
        }

        token.setUsed(true);
        otpTokenRepository.save(token);
    }

    @Transactional
    public LoginResponse resetPassword(ResetPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());
        String errorTitle = "Reset password";
        User user = findUserOrThrow(email, errorTitle);

        if (user.getStatus() != UserStatus.OTP_PASSWORD_RECOVERY) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Non è possibile reimpostare la password in questo stato. CODICE: AU220",
                    null, HttpStatus.BAD_REQUEST);
        }

        // Verifica che l'OTP sia già stato validato (nessun token attivo rimasto)
        if (otpTokenRepository.existsByUserIdAndTokenTypeAndUsedFalse(user.getId(), OtpTokenType.PASSWORD_RESET)) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Devi prima verificare il codice OTP. CODICE: AU221",
                    null, HttpStatus.BAD_REQUEST);
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < PASSWORD_MIN_LENGTH) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "La password deve contenere almeno " + PASSWORD_MIN_LENGTH + " caratteri. CODICE: AU222",
                    null, HttpStatus.BAD_REQUEST);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        return LoginResponse.fromUser(user);
    }

    // ========================================
    //            Metodi privati
    // ========================================

    private OtpToken buildOtpToken(java.util.UUID userId, OtpTokenType type) {
        return OtpToken.builder()
                .userId(userId)
                .tokenType(type)
                .otpCode(generateOtp())
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build();
    }

    private String generateOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private void sendOtpEmail(String recipientEmail, String otpCode) {
        String title = "Codice recupero password";
        String body = """
                <p>Hai richiesto di reimpostare la password del tuo account Campusly.</p>
                <p>Usa il codice qui sotto per completare la procedura:</p>
                <div style=\"margin: 28px 0; text-align: center; font-size: 36px; font-weight: 700; letter-spacing: 8px; color: #6C63FF;\">%s</div>
                <p style=\"color: #6B7280; font-size: 13px;\">Il codice è valido per <strong>%d minuti</strong>.</p>
                """.formatted(otpCode, OTP_EXPIRY_MINUTES);
        emailService.sendHtmlEmail(
                recipientEmail,
                "Codice recupero password Campusly",
                List.of(),
                title,
                body);
    }

    private User findUserOrThrow(String email, String errorTitle) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> ExceptionBackend.fromError(
                        errorTitle,
                        "Nessun account trovato con questa email. CODICE: AU200",
                        null, HttpStatus.NOT_FOUND));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
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
