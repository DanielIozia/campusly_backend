package com.campusly.campusly_backend.actors.user.interfaces.user;

import com.campusly.campusly_backend.shared.exception.ExceptionBackend;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public record UserUniversityRequest(UUID universityId) {

    public void validate(String errorTitle) {
        if (universityId == null) {
            throw ExceptionBackend.fromError(errorTitle,
                    "L'ID università è obbligatorio. CODICE: US001", null, HttpStatus.BAD_REQUEST);
        }
    }
}
