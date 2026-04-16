package com.campusly.campusly_backend.actors.university.interfaces;

import com.campusly.campusly_backend.shared.exception.ExceptionBackend;
import org.springframework.http.HttpStatus;

public record UniversityRequest(
        String name,
        String shortName,
        String city,
        String country,
        String emailDomain,
        String websiteUrl,
        Boolean international
) {
    public void validate(String errorTitle) {
        if (name == null || name.isBlank()) {
            throw ExceptionBackend.fromError(errorTitle,
                    "Il nome è obbligatorio. CODICE: UN001", null, HttpStatus.BAD_REQUEST);
        }
        if (name.length() > 255) {
            throw ExceptionBackend.fromError(errorTitle,
                    "Il nome non può superare 255 caratteri. CODICE: UN002", null, HttpStatus.BAD_REQUEST);
        }
        if (city == null || city.isBlank()) {
            throw ExceptionBackend.fromError(errorTitle,
                    "La città è obbligatoria. CODICE: UN003", null, HttpStatus.BAD_REQUEST);
        }
        if (country == null || country.isBlank()) {
            throw ExceptionBackend.fromError(errorTitle,
                    "Il paese è obbligatorio. CODICE: UN004", null, HttpStatus.BAD_REQUEST);
        }
    }
}
