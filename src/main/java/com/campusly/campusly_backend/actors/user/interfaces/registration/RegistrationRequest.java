package com.campusly.campusly_backend.actors.user.interfaces.registration;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import com.campusly.campusly_backend.shared.exception.ExceptionBackend;
import com.campusly.campusly_backend.shared.validation.ValidPhoneNumber;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {

    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
    private BirthDate birthDate;
    @ValidPhoneNumber
    private String phone;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BirthDate {
        private int day;
        private int month;
        private int year;

        public void isValid() throws ExceptionBackend {

            String errorTitle = "Errore data di nascita";

            if (day < 1 || day > 31) {
                throw ExceptionBackend.fromError(
                        errorTitle,
                        "Giorno di nascita non valido. CODICE: RE100",
                        this, HttpStatus.BAD_REQUEST);
            }
            if (month < 1 || month > 12) {
                throw ExceptionBackend.fromError(
                        errorTitle,
                        "Mese di nascita non valido. CODICE: RE100",
                        this, HttpStatus.BAD_REQUEST);
            }
            if (year < 1900 || year > java.time.LocalDate.now().getYear()) {
                throw ExceptionBackend.fromError(
                        errorTitle,
                        "Anno di nascita non valido. CODICE: RE100",
                        this, HttpStatus.BAD_REQUEST);
            }

            // Check per febbraio e anni bisestili
            if (month == 2) {
                if (java.time.Year.isLeap(year)) {
                    if (day > 29) {
                        throw ExceptionBackend.fromError(
                                errorTitle,
                                "Febbraio può avere massimo 29 giorni negli anni bisestili. CODICE: RE100",
                                this, HttpStatus.BAD_REQUEST);
                    }
                } else {
                    if (day > 28) {
                        throw ExceptionBackend.fromError(
                                errorTitle,
                                "Febbraio può avere massimo 28 giorni negli anni non bisestili. CODICE: RE100",
                                this, HttpStatus.BAD_REQUEST);
                    }
                }
            }
        }

        public LocalDate toLocalDate() {
            return LocalDate.of(year, month, day);
        }
    }

    public void isValid(String errorTitle) throws ExceptionBackend {

        if (firstName == null || firstName.isBlank()) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Nome obbligatorio. CODICE: RE100",
                    null, HttpStatus.BAD_REQUEST);
        }

        if (lastName == null || lastName.isBlank()) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Cognome obbligatorio. CODICE: RE100",
                    null, HttpStatus.BAD_REQUEST);
        }

        if (username == null || username.isBlank()) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Username obbligatorio. CODICE: RE100",
                    null, HttpStatus.BAD_REQUEST);
        }

        if (email == null || email.isBlank()) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Email obbligatoria. CODICE: RE100",
                    null, HttpStatus.BAD_REQUEST);
        }

        if (password == null || password.isBlank()) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Password obbligatoria. CODICE: RE100",
                    null, HttpStatus.BAD_REQUEST);
        }

        if (birthDate == null) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Data di nascita obbligatoria. CODICE: RE100",
                    null, HttpStatus.BAD_REQUEST);
        }
    }

}
