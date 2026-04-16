package com.campusly.campusly_backend.actors.university.controllers;

import com.campusly.campusly_backend.actors.university.interfaces.UniversityRequest;
import com.campusly.campusly_backend.actors.university.services.UniversityService;
import com.campusly.campusly_backend.shared.exception.CustomResponse;
import com.campusly.campusly_backend.shared.exception.ExceptionUtilService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UniversityController {

    private final UniversityService universityService;
    private final ExceptionUtilService exceptionUtilService;

    // ---------------------------------------------------------------
    // Endpoint pubblici (autenticati)
    // ---------------------------------------------------------------

    @GetMapping("/universities")
    public ResponseEntity<?> getAll(HttpServletRequest request) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            customResponse.setData(universityService.getAll());
            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }

    @GetMapping("/universities/{id}")
    public ResponseEntity<?> getById(HttpServletRequest request, @PathVariable UUID id) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            customResponse.setData(universityService.getById(id));
            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }

    // ---------------------------------------------------------------
    // Endpoint admin (SUPER_ADMIN)
    // ---------------------------------------------------------------

    @PostMapping("/admin/universities")
    public ResponseEntity<?> create(HttpServletRequest request, @RequestBody UniversityRequest body) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            customResponse.setData(universityService.create(body));
            return ResponseEntity.status(HttpStatus.CREATED).body(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }

    @PutMapping("/admin/universities/{id}")
    public ResponseEntity<?> update(
            HttpServletRequest request,
            @PathVariable UUID id,
            @RequestBody UniversityRequest body) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            customResponse.setData(universityService.update(id, body));
            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }

    @DeleteMapping("/admin/universities/{id}")
    public ResponseEntity<?> delete(HttpServletRequest request, @PathVariable UUID id) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            universityService.delete(id);
            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }
}
