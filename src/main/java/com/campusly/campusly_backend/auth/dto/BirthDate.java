package com.campusly.campusly_backend.auth.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO per la data di nascita suddivisa in giorno, mese e anno.
 */
public record BirthDate(

        @NotNull(message = "Il giorno è obbligatorio")
        Long day,

        @NotNull(message = "Il mese è obbligatorio")
        Long month,

        @NotNull(message = "L'anno è obbligatorio")
        Long year

) {
    /**
     * Converte in LocalDate.
     */
    public LocalDate toLocalDate() {
        return LocalDate.of(year.intValue(), month.intValue(), day.intValue());
    }
}
