package com.campusly.campusly_backend.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ExceptionBackend.class)
    public ResponseEntity<CustomResponse> handleExceptionBackend(ExceptionBackend ex,
            HttpServletRequest request) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        ErrorDetail errorDetail = ex.getErrorDetail();

        if (ex.isWarning()) {
            customResponse.setWarning(errorDetail);
        } else {
            customResponse.setError(errorDetail);
        }

        ResponseEntity.BodyBuilder builder = ResponseEntity.status(ex.getStatus());

        if (ex.getStatus() == HttpStatus.UNAUTHORIZED) {
            ResponseCookie cookie = ResponseCookie.from("token", "")
                    .maxAge(0)
                    .path("/")
                    .httpOnly(true)
                    .secure(true)
                    .build();
            builder.header(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return builder.body(customResponse);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String httpMethod = extractHttpMethod(request);
        CustomResponse customResponse = new CustomResponse(httpMethod);
        customResponse.setError(new ErrorDetail(
                "Risorsa Non Trovata",
                "Nessun endpoint trovato per " + httpMethod + " " + ex.getRequestURL(),
                null));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(customResponse);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String httpMethod = extractHttpMethod(request);

        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validazione fallita");

        CustomResponse customResponse = new CustomResponse(httpMethod);
        customResponse.setError(new ErrorDetail("Errore di Validazione", detail, null));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(customResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomResponse> handleGenericException(Exception ex,
            HttpServletRequest request) {
        log.error("Errore interno non gestito: {}", ex.getMessage(), ex);

        CustomResponse customResponse = new CustomResponse(request.getMethod());
        customResponse.setError(new ErrorDetail(
                "Errore Interno del Server",
                "Si è verificato un errore imprevisto. Contattare il supporto tecnico.",
                null));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(customResponse);
    }

    private String extractHttpMethod(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getHttpMethod().name();
        }
        return "UNKNOWN";
    }
}
