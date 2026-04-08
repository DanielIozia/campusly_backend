package com.campusly.campusly_backend.auth.controller;

import com.campusly.campusly_backend.auth.dto.*;
import com.campusly.campusly_backend.auth.service.AuthService;
import com.campusly.campusly_backend.shared.exception.CustomResponse;
import com.campusly.campusly_backend.shared.exception.ExceptionUtilService;
import com.campusly.campusly_backend.shared.security.JwtService;
import jakarta.servlet.http.Cookie;
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
    private final JwtService jwtService;
    private final ExceptionUtilService exceptionUtilService;

    @PostMapping("/register")
    public ResponseEntity<?> register(HttpServletRequest request,
            HttpServletResponse response,
            @Valid @RequestBody RegisterRequest registerRequest) {
        UUID authenticatedUserId = null;
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            UserAuthResponse data = authService.register(registerRequest, response);
            customResponse.setData(data);

            return ResponseEntity.status(HttpStatus.CREATED).body(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(
                    e, request, authenticatedUserId, null, customResponse.getMethod());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest request,
            HttpServletResponse response,
            @Valid @RequestBody LoginRequest loginRequest) {
        UUID authenticatedUserId = null;
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            UserAuthResponse data = authService.login(loginRequest, response);
            customResponse.setData(data);

            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(
                    e, request, authenticatedUserId, null, customResponse.getMethod());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        UUID authenticatedUserId = null;
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            authenticatedUserId = jwtService.getUserIdFromRequest(request);

            UserProfileResponse data = authService.getAuthenticatedUser(authenticatedUserId);
            customResponse.setData(data);

            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(
                    e, request, authenticatedUserId, null, customResponse.getMethod());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        UUID authenticatedUserId = null;
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            authenticatedUserId = jwtService.getUserIdFromRequest(request);

            String token = jwtService.getTokenFromCookie(request);
            if (token != null) {
                authService.logout(token);
            }

            Cookie cookie = new Cookie("token", "");
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            cookie.setSecure(jwtService.isSecure());
            cookie.setAttribute("SameSite", jwtService.getSameSiteAttribute());
            response.addCookie(cookie);

            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(
                    e, request, authenticatedUserId, null, customResponse.getMethod());
        }
    }
}
