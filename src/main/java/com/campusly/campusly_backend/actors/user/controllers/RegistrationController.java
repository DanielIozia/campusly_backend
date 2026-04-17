package com.campusly.campusly_backend.actors.user.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusly.campusly_backend.actors.user.interfaces.registration.RegistrationCompleteRequest;
import com.campusly.campusly_backend.actors.user.interfaces.registration.RegistrationInitRequest;
import com.campusly.campusly_backend.actors.user.interfaces.registration.RegistrationOtpRequest;
import com.campusly.campusly_backend.actors.user.interfaces.registration.RegistrationResendOtpRequest;
import com.campusly.campusly_backend.actors.user.services.RegistrationService;
import com.campusly.campusly_backend.actors.user.interfaces.auth.LoginResponse;
import com.campusly.campusly_backend.shared.exception.CustomResponse;
import com.campusly.campusly_backend.shared.exception.ExceptionUtilService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final ExceptionUtilService exceptionUtilService;

    // ==================================================================
    // Registrazione — flusso a 3 step (email + OTP + completamento dati)
    // ==================================================================

    // * Step 1: l'utente invia l'email e riceve il codice OTP.
    @PostMapping("/send-otp")
    public ResponseEntity<?> registerInit(HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody RegistrationInitRequest body) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            registrationService.sendOtp(body);
            customResponse.setData(null);
            return ResponseEntity.status(HttpStatus.CREATED).body(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }

    // * Step 2: l'utente inserisce il codice OTP ricevuto via email.
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody RegistrationOtpRequest body) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            registrationService.verifyOtp(body);
            customResponse.setData(null);
            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }

    // * Step 3: l'utente inserisce nome, cognome, username, password e data di nascita.
    @PostMapping("/complete")
    public ResponseEntity<?> registerComplete(HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody RegistrationCompleteRequest body) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            LoginResponse data = registrationService.completeRegistration(body, response);
            customResponse.setData(data);
            return ResponseEntity.status(HttpStatus.CREATED).body(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }

    // * Reinvio OTP: disponibile solo se l'utente è in stato CODE_VERIFICATION.
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody RegistrationResendOtpRequest body) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            registrationService.resendOtp(body);
            customResponse.setData(null);
            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }

}
