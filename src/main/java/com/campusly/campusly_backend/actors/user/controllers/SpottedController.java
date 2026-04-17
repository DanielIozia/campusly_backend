package com.campusly.campusly_backend.actors.user.controllers;

import com.campusly.campusly_backend.actors.user.interfaces.spotted.SpottedCreateRequest;
import com.campusly.campusly_backend.actors.user.interfaces.spotted.SpottedFiltersRequest;
import com.campusly.campusly_backend.actors.user.interfaces.spotted.SpottedUpdateRequest;
import com.campusly.campusly_backend.actors.user.services.SpottedService;
import com.campusly.campusly_backend.shared.exception.CustomResponse;
import com.campusly.campusly_backend.shared.exception.ExceptionUtilService;
import com.campusly.campusly_backend.shared.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/spotted")
public class SpottedController {

    private final SpottedService spottedService;
    private final ExceptionUtilService exceptionUtilService;
    private final JwtService jwtService;

    // =========================
    // Crea uno spotted
    // =========================
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            HttpServletRequest request,
            @RequestParam("content") String content,
            @RequestParam("category") String category,
            @RequestParam(value = "isAnonymous", defaultValue = "false") Boolean isAnonymous,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            UUID userId = jwtService.getUserIdFromRequest(request);
            SpottedCreateRequest data = new SpottedCreateRequest(content, category, isAnonymous);
            customResponse.setData(spottedService.create(data, images, userId));
            return ResponseEntity.status(HttpStatus.CREATED).body(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }

    // =========================
    // Ottieni uno spotted per ID
    // =========================
    @GetMapping("/get")
    public ResponseEntity<?> getById(HttpServletRequest request, @RequestParam UUID id) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            customResponse.setData(spottedService.getById(id));
            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }

    // =========================
    // Ottieni spotted per università
    // =========================
    @PostMapping("/list")
    public ResponseEntity<?> listAll(
            HttpServletRequest request,
            @RequestBody(required = false) SpottedFiltersRequest filters) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            customResponse.setData(spottedService.listAll(filters));
            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }

    // =========================
    // Modifica uno spotted
    // =========================
        @PutMapping(value = "/modify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<?> update(
            HttpServletRequest request,
            @RequestParam UUID id,
            @RequestParam("content") String content,
            @RequestParam("category") String category,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            UUID userId = jwtService.getUserIdFromRequest(request);
            SpottedUpdateRequest data = new SpottedUpdateRequest(content, category);
            customResponse.setData(spottedService.update(id, data, images, userId));
            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }

    // =========================
    // Elimina uno spotted
    // =========================
    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(HttpServletRequest request, @RequestParam UUID id) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            UUID userId = jwtService.getUserIdFromRequest(request);
            spottedService.delete(id, userId);
            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }

    // =========================
    // Elimina un'immagine di uno spotted
    // =========================
        @DeleteMapping("/delete-image")
        public ResponseEntity<?> deleteImage(
            HttpServletRequest request,
            @RequestParam UUID id,
            @RequestParam UUID imageId) {
        CustomResponse customResponse = new CustomResponse(request.getMethod());
        try {
            UUID userId = jwtService.getUserIdFromRequest(request);
            spottedService.deleteImage(id, imageId, userId);
            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            return exceptionUtilService.handleAnyException(e, request, null, null, customResponse.getMethod());
        }
    }

}
