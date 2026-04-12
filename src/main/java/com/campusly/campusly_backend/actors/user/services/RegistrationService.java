package com.campusly.campusly_backend.actors.user.services;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.campusly.campusly_backend.actors.user.interfaces.auth.LoginResponse;
import com.campusly.campusly_backend.actors.user.interfaces.registration.RegistrationCompleteRequest;
import com.campusly.campusly_backend.actors.user.interfaces.registration.RegistrationInitRequest;
import com.campusly.campusly_backend.actors.user.interfaces.registration.RegistrationOtpRequest;
import com.campusly.campusly_backend.actors.user.interfaces.registration.RegistrationResendOtpRequest;
import com.campusly.campusly_backend.actors.user.interfaces.registration.enums.UserStatus;
import com.campusly.campusly_backend.auth.entity.AuthProvider;
import com.campusly.campusly_backend.auth.entity.Role;
import com.campusly.campusly_backend.database.entity.User;
import com.campusly.campusly_backend.database.repository.UserRepository;
import com.campusly.campusly_backend.shared.email.EmailService;
import com.campusly.campusly_backend.shared.exception.ExceptionBackend;
import com.campusly.campusly_backend.shared.security.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    private static final int MIN_AGE = 16;
    private static final int OTP_EXPIRY_MINUTES = 5; // scadenza otp in minuti
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // ---------------------------------------------------------------
    // Step 1 — sendOtp: inserisce email, invia OTP
    // ---------------------------------------------------------------

    @Transactional
    public void sendOtp(RegistrationInitRequest request) {
        request.isValid();
        String email = normalizeEmail(request.getEmail());
        String errorTitle = "Errore registrazione";

        Optional<User> existing = userRepository.findByEmail(email);

        if (existing.isPresent()) {
            User user = existing.get();

            // Utente già attivo: account completato in precedenza
            if (user.getStatus() == UserStatus.ACTIVE) {
                if (user.getAuthProvider() == AuthProvider.GOOGLE) {
                    throw ExceptionBackend.fromError(
                            errorTitle,
                            "Questo indirizzo email è associato a un account Google. Accedi con Google. CODICE: AU111",
                            null, HttpStatus.CONFLICT);
                }
                throw ExceptionBackend.fromError(
                        errorTitle,
                        "Esiste già un account attivo con questa email. CODICE: AU111",
                        null, HttpStatus.CONFLICT);
            }

            // Utente già in attesa del codice: deve usare l'endpoint di reinvio
            if (user.getStatus() == UserStatus.CODE_VERIFICATION) {
                throw ExceptionBackend.fromError(
                        errorTitle,
                        "Un codice OTP è già stato inviato a questa email. Usa l'apposita funzione di reinvio se non lo hai ricevuto. CODICE: AU112",
                        null, HttpStatus.CONFLICT);
            }

            // Utente in stato SIGNUP (aveva verificato il codice ma non ha completato il
            // profilo):
            // generiamo un nuovo OTP e lo rimandiamo in CODE_VERIFICATION
            if (user.getStatus() == UserStatus.SIGNUP) {
                String otp = generateOtp();
                user.setOtpCode(otp);
                user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
                user.setStatus(UserStatus.CODE_VERIFICATION);
                userRepository.save(user);
                sendOtpEmail(email, otp);
                return;
            }
        }

        // Nuova email: crea il record utente con solo l'email e invia OTP
        String otp = generateOtp();
        User newUser = User.builder()
                .email(email)
                .status(UserStatus.CODE_VERIFICATION)
                .otpCode(otp)
                .otpExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .authProvider(AuthProvider.LOCAL)
                .role(Role.CAMPUSLY_USER)
                .build();

        userRepository.save(newUser);
        sendOtpEmail(email, otp);
        log.info("Nuovo utente creato in CODE_VERIFICATION: {}", email);
    }

    // ---------------------------------------------------------------
    // Step 2 — Verifica OTP
    // ---------------------------------------------------------------

    @Transactional
    public void verifyOtp(RegistrationOtpRequest request) {
        request.isValid();
        String email = normalizeEmail(request.getEmail());
        String errorTitle = "Errore verifica OTP";

        User user = findUserOrThrow(email, errorTitle);

        // L'utente deve essere in attesa del codice
        if (user.getStatus() != UserStatus.CODE_VERIFICATION) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Nessun codice OTP in attesa di verifica per questa email. CODICE: AU113",
                    null, HttpStatus.BAD_REQUEST);
        }

        // Controlla scadenza
        if (user.getOtpExpiresAt() == null || LocalDateTime.now().isAfter(user.getOtpExpiresAt())) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Il codice OTP è scaduto. Richiedi un nuovo codice. CODICE: AU114",
                    null, HttpStatus.GONE);
        }

        // Controlla correttezza codice
        if (!user.getOtpCode().equals(request.getOtpCode().trim())) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Codice OTP non valido. CODICE: AU115",
                    null, HttpStatus.BAD_REQUEST);
        }

        // OTP corretto: avanza a SIGNUP e pulisce i campi OTP
        user.setStatus(UserStatus.SIGNUP);
        user.setOtpCode(null);
        user.setOtpExpiresAt(null);
        userRepository.save(user);
        log.info("OTP verificato con successo, utente avanzato a SIGNUP: {}", email);
    }

    // ---------------------------------------------------------------
    // Step 3 — Completamento profilo
    // ---------------------------------------------------------------

    @Transactional
    public LoginResponse completeRegistration(RegistrationCompleteRequest request, HttpServletResponse response) {
        request.isValid();
        String email = normalizeEmail(request.getEmail());
        String errorTitle = "Errore completamento registrazione";

        User user = findUserOrThrow(email, errorTitle);

        // Deve essere in stato SIGNUP
        if (user.getStatus() != UserStatus.SIGNUP) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Non è possibile completare la registrazione: lo stato dell'account non è valido. CODICE: AU116",
                    null, HttpStatus.BAD_REQUEST);
        }

        // Username già in uso da un altro utente
        if (userRepository.existsByUsername(request.getUsername())) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Username non disponibile. CODICE: AU117",
                    null, HttpStatus.CONFLICT);
        }

        // Validazione età minima
        LocalDate birthDate = request.getBirthDate().toLocalDate();
        int eta = Period.between(birthDate, LocalDate.now()).getYears();
        if (eta < MIN_AGE) {
            throw ExceptionBackend.fromError(
                    "Età non consentita",
                    "Devi avere almeno " + MIN_AGE + " anni per registrarti. CODICE: AU101",
                    null, HttpStatus.BAD_REQUEST);
        }

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setUsername(request.getUsername().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setBirthDate(birthDate);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);
        addTokenCookie(user, response);
        log.info("Registrazione completata, utente ACTIVE: {}", email);
        return LoginResponse.fromUser(user);
    }

    // ---------------------------------------------------------------
    // Reinvio OTP — solo se l'utente è in CODE_VERIFICATION
    // ---------------------------------------------------------------

    @Transactional
    public void resendOtp(RegistrationResendOtpRequest request) {
        request.isValid();
        String email = normalizeEmail(request.getEmail());
        String errorTitle = "Errore reinvio OTP";

        User user = findUserOrThrow(email, errorTitle);

        // Il reinvio è consentito solo in CODE_VERIFICATION
        if (user.getStatus() != UserStatus.CODE_VERIFICATION) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Il reinvio del codice non è disponibile per questo account. CODICE: AU118",
                    null, HttpStatus.BAD_REQUEST);
        }

        String otp = generateOtp();
        user.setOtpCode(otp);
        user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        userRepository.save(user);
        sendOtpEmail(email, otp);
        log.info("OTP reinviato a: {}", email);
    }

    // ──────────────────────────────────────────────────────
    // Utility
    // ──────────────────────────────────────────────────────
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private void sendOtpEmail(String recipientEmail, String otpCode) {
        String title = "Il tuo codice di verifica";
        String body = """
                <p>Benvenuto su <strong>Campusly</strong>!</p>
                <p>Usa il codice qui sotto per completare la verifica del tuo account:</p>
                <div style="
                    margin: 28px 0;
                    text-align: center;
                    font-size: 36px;
                    font-weight: 700;
                    letter-spacing: 8px;
                    color: #6C63FF;
                ">%s</div>
                <p style="color: #6B7280; font-size: 13px;">
                    Il codice è valido per <strong>%d minuti</strong>.
                </p>
                """.formatted(otpCode, OTP_EXPIRY_MINUTES);

        emailService.sendHtmlEmail(
                recipientEmail,
                "Codice di verifica Campusly",
                List.of(),
                title,
                body);
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

    private String generateOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    // Recupera l'utente per email o lancia un'eccezione NOT_FOUND uniforme.
    private User findUserOrThrow(String email, String errorTitle) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> ExceptionBackend.fromError(
                        errorTitle,
                        "Nessun account trovato con questa email. CODICE: AU200",
                        null, HttpStatus.NOT_FOUND));
    }
}
