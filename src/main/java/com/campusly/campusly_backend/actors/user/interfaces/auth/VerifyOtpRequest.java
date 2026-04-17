package com.campusly.campusly_backend.actors.user.interfaces.auth;

import org.springframework.http.HttpStatus;

import com.campusly.campusly_backend.shared.exception.ExceptionBackend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyOtpRequest {
    private String email;
    private String otpCode;

    public void isValid(String errorTitle) throws ExceptionBackend {
        if (email == null || email.isBlank()) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Email obbligatoria. CODICE: AU200",
                    null, HttpStatus.BAD_REQUEST);
        }
        if (otpCode == null || otpCode.isBlank()) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "OTP obbligatorio. CODICE: AU201",
                    null, HttpStatus.BAD_REQUEST);
        }
    }
}
