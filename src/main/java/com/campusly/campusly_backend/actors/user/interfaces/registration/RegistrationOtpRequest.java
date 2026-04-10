package com.campusly.campusly_backend.actors.user.interfaces.registration;

import org.springframework.http.HttpStatus;

import com.campusly.campusly_backend.shared.exception.ExceptionBackend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationOtpRequest {

    private String email;
    private String otpCode;

    public void isValid() throws ExceptionBackend {
        if (email == null || email.isBlank()) {
            throw ExceptionBackend.fromError(
                    "Errore verifica OTP",
                    "Email obbligatoria. CODICE: AU110",
                    null, HttpStatus.BAD_REQUEST);
        }
        if (otpCode == null || otpCode.isBlank()) {
            throw ExceptionBackend.fromError(
                    "Errore verifica OTP",
                    "Codice OTP obbligatorio. CODICE: AU113",
                    null, HttpStatus.BAD_REQUEST);
        }
    }
}
