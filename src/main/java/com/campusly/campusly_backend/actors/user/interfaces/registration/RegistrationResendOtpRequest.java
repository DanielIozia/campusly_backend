package com.campusly.campusly_backend.actors.user.interfaces.registration;

import org.springframework.http.HttpStatus;

import com.campusly.campusly_backend.shared.exception.ExceptionBackend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResendOtpRequest {

    private String email;

    public void isValid() throws ExceptionBackend {
        if (email == null || email.isBlank()) {
            throw ExceptionBackend.fromError(
                    "Errore reinvio OTP",
                    "Email obbligatoria. CODICE: AU110",
                    null, HttpStatus.BAD_REQUEST);
        }
    }
}
