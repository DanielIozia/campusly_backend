package com.campusly.campusly_backend.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/*
    * Valida il numero di telefono nel formato internazionale con trattino:
    * +{prefisso}-{numero}  →  es. +39-3471234567
    
    * - Prefisso: 1-4 cifre dopo il +
    * - Separatore: trattino (-)
    * - Numero: 6-15 cifre
 */
@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {

    String message() default "Numero di telefono non valido. Formato richiesto: +{prefisso}-{numero} (es. +39-3471234567)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
