package com.campusly.campusly_backend.actors.user.controllers;

import com.campusly.campusly_backend.actors.user.interfaces.user.UserUniversityRequest;
import com.campusly.campusly_backend.actors.user.services.UserService;
import com.campusly.campusly_backend.shared.exception.CustomResponse;
import com.campusly.campusly_backend.shared.exception.ExceptionUtilService;
import com.campusly.campusly_backend.shared.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final ExceptionUtilService exceptionUtilService;
    private final JwtService jwtService;

    //* Associa o modifica l'università dell'utente autenticato
    @PutMapping("/me/university")
    public ResponseEntity<?> updateUniversity(
            HttpServletRequest request,
            @RequestBody UserUniversityRequest body) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            UUID userId = jwtService.getUserIdFromRequest(request);
            customResponse.setData(userService.updateUniversity(body, userId));
            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }
}
