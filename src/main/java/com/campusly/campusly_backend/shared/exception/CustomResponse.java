package com.campusly.campusly_backend.shared.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomResponse {
    
    private Object data;
    private final String method;
    private ErrorDetail error;
    private ErrorDetail warning;

    public CustomResponse(String method) {
        this.method = method;
    }
}
