package com.campusly.campusly_backend.actors.user.interfaces.auth;

import org.springframework.http.HttpStatus;

import com.campusly.campusly_backend.shared.exception.ExceptionBackend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    private String email;
    private String password;

    public void isValid(String errorTitle) throws ExceptionBackend {

        if (email == null || email.isBlank()) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Email obbligatoria. CODICE: AU200",
                    null, HttpStatus.BAD_REQUEST);
        }
        if (password == null || password.isBlank()) {
            throw ExceptionBackend.fromError(
                    errorTitle,
                    "Password obbligatoria. CODICE: AU201",
                    null, HttpStatus.BAD_REQUEST);
        }
    }
}
