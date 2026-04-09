package com.campusly.campusly_backend.auth.dto;

import com.campusly.campusly_backend.shared.validation.ValidPhoneNumber;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

/**
 * DTO per la registrazione di un utente standard (CAMPUSLY_USER).
 */
public record RegisterUserRequest(

        @NotBlank(message = "Il nome è obbligatorio")
        @Size(max = 100, message = "Il nome non può superare i 100 caratteri")
        String firstName,

        @NotBlank(message = "Il cognome è obbligatorio")
        @Size(max = 100, message = "Il cognome non può superare i 100 caratteri")
        String lastName,

        @NotBlank(message = "L'username è obbligatorio")
        @Size(min = 3, max = 50, message = "L'username deve essere tra 3 e 50 caratteri")
        @Pattern(regexp = "^[a-zA-Z0-9._]+$", message = "L'username può contenere solo lettere, numeri, punti e underscore")
        String username,

        @NotBlank(message = "L'email è obbligatoria")
        @Email(message = "Formato email non valido")
        String email,

        @NotBlank(message = "La password è obbligatoria")
        @Size(min = 8, message = "La password deve contenere almeno 8 caratteri")
        String password,

        @NotNull(message = "La data di nascita è obbligatoria")
        @Valid
        BirthDate birthDate,

        @NotBlank(message = "Il numero di telefono è obbligatorio")
        @ValidPhoneNumber
        String phone

) {}
