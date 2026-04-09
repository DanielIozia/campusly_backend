package com.campusly.campusly_backend.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Implementazione del validator per @ValidPhoneNumber.
 * Formato atteso: +{1-4 cifre}-{6-15 cifre}  →  es. +39-3471234567
 */
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+\\d{1,4}-\\d{6,15}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // null/blank delegato a @NotBlank
        }
        return PHONE_PATTERN.matcher(value).matches();
    }
}
