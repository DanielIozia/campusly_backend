package com.campusly.campusly_backend.actors.file.controllers;

import com.campusly.campusly_backend.shared.exception.CustomResponse;
import com.campusly.campusly_backend.shared.exception.ErrorDetail;
import com.campusly.campusly_backend.shared.exception.ExceptionBackend;
import com.campusly.campusly_backend.shared.storage.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final FileStorageService fileStorageService;

    @GetMapping("/{userId}/{fileName:.+}")
    public ResponseEntity<?> serveFile(
            HttpServletRequest request,
            @PathVariable String userId,
            @PathVariable String fileName) {
        try {
            String relativePath = userId + "/" + fileName;
            Resource resource = fileStorageService.loadAsResource(relativePath);

            String contentType = request.getServletContext().getMimeType(resource.getFilename());
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (ExceptionBackend e) {
            CustomResponse customResponse = new CustomResponse(request.getMethod());
            customResponse.setError(e.getErrorDetail());
            return ResponseEntity.status(e.getStatus()).body(customResponse);
        } catch (Exception e) {
            CustomResponse customResponse = new CustomResponse(request.getMethod());
            customResponse.setError(new ErrorDetail(
                    "Errore Interno",
                    "Impossibile recuperare il file. Contattare il supporto tecnico.",
                    null));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(customResponse);
        }
    }
}
