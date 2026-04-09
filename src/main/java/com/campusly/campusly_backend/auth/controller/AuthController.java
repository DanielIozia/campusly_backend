package com.campusly.campusly_backend.auth.controller;

import com.campusly.campusly_backend.auth.dto.*;
import com.campusly.campusly_backend.auth.service.AuthService;
import com.campusly.campusly_backend.shared.exception.CustomResponse;
import com.campusly.campusly_backend.shared.exception.ExceptionUtilService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ExceptionUtilService exceptionUtilService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(HttpServletRequest request,
            HttpServletResponse response,
            @Valid @RequestBody RegisterUserRequest registerRequest) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            UserAuthResponse data = authService.registerUser(registerRequest, response);
            customResponse.setData(data);

            return ResponseEntity.status(HttpStatus.CREATED).body(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(
                    e, request, null, null, customResponse.getMethod());
        }
    }

    @PostMapping("/register/creator")
    public ResponseEntity<?> registerCreator(HttpServletRequest request,
            HttpServletResponse response,
            @Valid @RequestBody RegisterCreatorRequest registerRequest) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            UserAuthResponse data = authService.registerCreator(registerRequest, response);
            customResponse.setData(data);

            return ResponseEntity.status(HttpStatus.CREATED).body(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(
                    e, request, null, null, customResponse.getMethod());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest request,
            HttpServletResponse response,
            @Valid @RequestBody LoginRequest loginRequest) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            UserAuthResponse data = authService.login(loginRequest, response);
            customResponse.setData(data);

            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(
                    e, request, null, null, customResponse.getMethod());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            UserProfileResponse data = authService.getMe();
            customResponse.setData(data);

            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(
                    e, request, null, null, customResponse.getMethod());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            authService.logout(request, response);

            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(
                    e, request, null, null, customResponse.getMethod());
        }
    }
}
