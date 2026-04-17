package com.campusly.campusly_backend.shared.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorDetail {
        private String title;
        private String message;
        private Object payload;
}