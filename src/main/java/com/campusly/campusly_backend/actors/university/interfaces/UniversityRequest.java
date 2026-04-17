package com.campusly.campusly_backend.actors.university.interfaces;

import com.campusly.campusly_backend.shared.exception.ExceptionBackend;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UniversityRequest {
    private String name;
    private String shortName;
    private String city;
    private String country;
    private String emailDomain;
    private String websiteUrl;
    private Boolean international;

    public void isValid(String errorTitle) {
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
