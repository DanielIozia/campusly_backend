package com.campusly.campusly_backend.actors.user.controllers;
import com.campusly.campusly_backend.actors.user.interfaces.auth.LoginRequest;
import com.campusly.campusly_backend.actors.user.interfaces.auth.LoginResponse;
import com.campusly.campusly_backend.actors.user.interfaces.auth.ForgotPasswordRequest;
import com.campusly.campusly_backend.actors.user.interfaces.auth.VerifyOtpRequest;
import com.campusly.campusly_backend.actors.user.interfaces.auth.ResetPasswordRequest;
import com.campusly.campusly_backend.actors.user.services.AuthService;
import com.campusly.campusly_backend.shared.exception.CustomResponse;
import com.campusly.campusly_backend.shared.exception.ExceptionUtilService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final ExceptionUtilService exceptionUtilService;


    //* Login: l'utente invia username e password, riceve access token e refresh token.
    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody LoginRequest loginRequest) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            LoginResponse data = authService.login(loginRequest, response);
            customResponse.setData(data);
            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }

    //* Me: l'utente ottiene le informazioni del proprio profilo.
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            customResponse.setData(authService.getMe());
            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }   

    //* Logout: invalidamento access token e refresh token.
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            authService.logout(request, response);
            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }

    
    // ===============================================
    //            Password Recovery Flow
    // ===============================================

    //* 1) L'utente invia la propria email
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(HttpServletRequest request, @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            authService.forgotPassword(forgotPasswordRequest);
            customResponse.setData(null);
            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }

    //* 2) L'utente riceve un OTP via email
    @PostMapping("/verify-password-otp")
    public ResponseEntity<?> verifyPasswordRecoveryOtp(HttpServletRequest request, @RequestBody VerifyOtpRequest verifyOtpRequest) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            authService.verifyPasswordRecoveryOtp(verifyOtpRequest);
            customResponse.setData(null);
            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }

    //* 3) L'utente invia la nuova password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(HttpServletRequest request, @RequestBody ResetPasswordRequest resetPasswordRequest) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            customResponse.setData(authService.resetPassword(resetPasswordRequest));
            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }
}
