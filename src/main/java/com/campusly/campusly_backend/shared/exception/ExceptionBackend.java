package com.campusly.campusly_backend.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ExceptionBackend extends RuntimeException {

    private final ErrorDetail ErrorDetail;
    private final HttpStatus status;
    private final boolean warning;

    private ExceptionBackend(ErrorDetail ErrorDetail, HttpStatus status, boolean warning) {
        super(ErrorDetail.getMessage());
        this.ErrorDetail = ErrorDetail;
        this.status = status;
        this.warning = warning;
    }

    public static ExceptionBackend fromError(String title, String message, Object payload, HttpStatus status) {
        return new ExceptionBackend(new ErrorDetail(title, message, payload), status, false);
    }

    public static ExceptionBackend fromWarning(String title, String message, Object payload, HttpStatus status) {
        return new ExceptionBackend(new ErrorDetail(title, message, payload), status, true);
    }
}
