package com.campusly.campusly_backend.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

/**
 * Gestore globale delle eccezioni per l'intera applicazione REST.
 * <p>
 * Estende {@link ResponseEntityExceptionHandler} per ereditare la gestione
 * predefinita delle eccezioni di Spring MVC, sovrascrivendo i metodi necessari
 * per restituire il DTO {@link ErrorResponse} standardizzato.
 * <p>
 * <strong>Nota su Spring Security:</strong>
 * Le eccezioni di autenticazione ({@code AuthenticationException}) e autorizzazione
 * ({@code AccessDeniedException}) vengono intercettate dai filtri di Spring Security
 * <em>prima</em> di raggiungere il DispatcherServlet, quindi non passano per questo handler.
 * Per gestirle con lo stesso formato JSON, è necessario implementare:
 * <ul>
 *   <li>{@code AuthenticationEntryPoint} — per errori 401 Unauthorized</li>
 *   <li>{@code AccessDeniedHandler} — per errori 403 Forbidden</li>
 * </ul>
 * Entrambi possono riutilizzare {@link ErrorResponse} per coerenza nel payload di errore.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ==================== Eccezioni Custom ====================

    /**
     * Gestisce {@link ResourceNotFoundException} — HTTP 404.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex,
                                                                HttpServletRequest request) {
        log.warn("Risorsa non trovata: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                request.getMethod(),
                HttpStatus.NOT_FOUND.value(),
                "Risorsa Non Trovata",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Gestisce {@link BadRequestException} — HTTP 400.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex,
                                                          HttpServletRequest request) {
        log.warn("Richiesta non valida: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                request.getMethod(),
                HttpStatus.BAD_REQUEST.value(),
                "Richiesta Non Valida",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ==================== Eccezioni Spring MVC ====================

    /**
     * Gestisce {@link MethodArgumentNotValidException} — errori di validazione @Valid.
     * Sovrascrive il metodo di {@link ResponseEntityExceptionHandler} per restituire
     * il DTO standardizzato con la lista dettagliata degli errori per campo.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        String httpMethod = extractHttpMethod(request);

        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ErrorResponse.ValidationError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .toList();

        String detail = String.format("Validazione fallita: %d errore/i riscontrato/i", validationErrors.size());

        ErrorResponse error = ErrorResponse.of(
                httpMethod,
                HttpStatus.BAD_REQUEST.value(),
                "Errore di Validazione",
                detail,
                validationErrors
        );

        log.warn("Validazione fallita: {}", validationErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ==================== Fallback Generico ====================

    /**
     * Gestisce tutte le eccezioni non catturate da handler più specifici — HTTP 500.
     * Logga l'intero stack trace per facilitare il debug, ma restituisce al client
     * un messaggio generico per non esporre dettagli interni.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex,
                                                                HttpServletRequest request) {
        log.error("Errore interno non gestito: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.of(
                request.getMethod(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Errore Interno del Server",
                "Si è verificato un errore imprevisto. Contattare il supporto tecnico."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // ==================== Utility ====================

    /**
     * Estrae il metodo HTTP dal {@link WebRequest} (utilizzato nei metodi ereditati
     * da {@link ResponseEntityExceptionHandler} che non ricevono {@link HttpServletRequest}).
     */
    private String extractHttpMethod(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getHttpMethod().name();
        }
        return "UNKNOWN";
    }
}
