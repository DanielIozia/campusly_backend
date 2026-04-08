package com.campusly.campusly_backend.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class ExceptionUtilService {

    public ResponseEntity<CustomResponse> handleAnyException(
            Exception e,
            HttpServletRequest request,
            UUID authenticatedUserId,
            String context,
            String method
    ) {
        CustomResponse customResponse = new CustomResponse(method);

        if (e instanceof ExceptionBackend ex) {
            ErrorDetail errorDetail = ex.getErrorDetail();

            if (ex.isWarning()) {
                customResponse.setWarning(errorDetail);
            } else {
                customResponse.setError(errorDetail);
            }

            return ResponseEntity.status(ex.getStatus()).body(customResponse);
        }

        // Fallback per eccezioni non previste
        log.error("Errore imprevisto — userId: {}, context: {}, uri: {}",
                authenticatedUserId, context, request.getRequestURI(), e);

        customResponse.setError(new ErrorDetail(
                "Errore Interno",
                "Si è verificato un errore imprevisto. Contattare il supporto tecnico.",
                null
        ));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(customResponse);
    }
}
