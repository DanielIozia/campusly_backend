package com.campusly.campusly_backend.actors.user.interfaces.spotted;

import com.campusly.campusly_backend.shared.exception.ExceptionBackend;
import org.springframework.http.HttpStatus;

public record SpottedCreateRequest(
        String content,
        String category,
        Boolean isAnonymous
) {
    public void validate(String errorTitle) {
        if (content == null || content.isBlank()) {
            throw ExceptionBackend.fromError(errorTitle,
                    "Il contenuto non può essere vuoto. CODICE: SP001", null, HttpStatus.BAD_REQUEST);
        }
        if (content.length() > 1000) {
            throw ExceptionBackend.fromError(errorTitle,
                    "Il contenuto non può superare 1000 caratteri. CODICE: SP002", null, HttpStatus.BAD_REQUEST);
        }
        if (category == null || category.isBlank()) {
            throw ExceptionBackend.fromError(errorTitle,
                    "La categoria è obbligatoria. CODICE: SP003", null, HttpStatus.BAD_REQUEST);
        }
        if (category.length() > 30) {
            throw ExceptionBackend.fromError(errorTitle,
                    "La categoria non può superare 30 caratteri. CODICE: SP004", null, HttpStatus.BAD_REQUEST);
        }
    }
}
