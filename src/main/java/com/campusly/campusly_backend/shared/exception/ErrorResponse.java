package com.campusly.campusly_backend.shared.exception;

import java.time.Instant;
import java.util.List;

/**
 * DTO standardizzato per tutte le risposte di errore dell'API.
 * Utilizza un Java Record (Java 17+) per massima concisione e immutabilità.
 *
 * @param httpMethod il metodo HTTP della richiesta (GET, POST, PUT, PATCH, DELETE)
 * @param status     il codice di stato HTTP (es. 400, 404, 500)
 * @param title      titolo sintetico dell'errore
 * @param content    messaggio di dettaglio dell'errore
 * @param errors     lista opzionale di errori di validazione campo-per-campo
 * @param timestamp  timestamp dell'errore
 */
public record ErrorResponse(
        String httpMethod,
        int status,
        String title,
        String content,
        List<ValidationError> errors,
        Instant timestamp
) {

    /**
     * Dettaglio di un singolo errore di validazione su un campo.
     *
     * @param field   il nome del campo che ha fallito la validazione
     * @param message il messaggio di errore associato
     */
    public record ValidationError(String field, String message) {
    }

    /**
     * Factory method per creare una risposta di errore senza errori di validazione.
     */
    public static ErrorResponse of(String httpMethod, int status, String title, String content) {
        return new ErrorResponse(httpMethod, status, title, content, List.of(), Instant.now());
    }

    /**
     * Factory method per creare una risposta di errore con errori di validazione.
     */
    public static ErrorResponse of(String httpMethod, int status, String title, String content,
                                   List<ValidationError> errors) {
        return new ErrorResponse(httpMethod, status, title, content, errors, Instant.now());
    }
}
