package com.campusly.campusly_backend.auth.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record RegisterRequest(

        @NotBlank(message = "Il nome è obbligatorio") 
        @Size(max = 100, message = "Il nome non può superare 100 caratteri") String name,

        @NotBlank(message = "Il cognome è obbligatorio") 
        @Size(max = 100, message = "Il cognome non può superare 100 caratteri") String surname,

        @NotNull(message = "La data di nascita è obbligatoria") 
        @Valid DateOfBirth dateOfBirth,

        @NotBlank(message = "L'email è obbligatoria") @Email(message = "Formato email non valido") String email,

        @NotBlank(message = "La password è obbligatoria") @Size(min = 8, max = 100, message = "La password deve essere tra 8 e 100 caratteri") String password,

        @Size(max = 20, message = "Il telefono non può superare 20 caratteri") String telephone) {

    /**
     * Oggetto per la data di nascita con giorno, mese e anno separati.
     */
    public record DateOfBirth(
            @NotNull(message = "Il giorno è obbligatorio") 
            @Min(value = 1, message = "Il giorno deve essere tra 1 e 31") 
            @Max(value = 31, message = "Il giorno deve essere tra 1 e 31") 
            Integer day,

            @NotNull(message = "Il mese è obbligatorio") 
            @Min(value = 1, message = "Il mese deve essere tra 1 e 12") 
            @Max(value = 12, message = "Il mese deve essere tra 1 e 12") 
            Integer month,

            @NotNull(message = "L'anno è obbligatorio") 
            @Min(value = 1900, message = "L'anno non può essere inferiore a 1900") 
            Integer year
        ) {
    }
}
