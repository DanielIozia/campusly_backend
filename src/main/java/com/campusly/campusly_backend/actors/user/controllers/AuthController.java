package com.campusly.campusly_backend.actors.user.controllers;
import org.springframework.web.bind.annotation.RequestBody;
import com.campusly.campusly_backend.actors.user.interfaces.auth.LoginRequest;
import com.campusly.campusly_backend.actors.user.interfaces.auth.LoginResponse;
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
public class AuthController {
    private final AuthService authService;
    private final ExceptionUtilService exceptionUtilService;


    //* Login: l'utente invia username e password, riceve access token e refresh token.
    @PostMapping("/auth/login")
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
    @GetMapping("/auth/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            authService.getMe();
            customResponse.setData(null);
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
}
