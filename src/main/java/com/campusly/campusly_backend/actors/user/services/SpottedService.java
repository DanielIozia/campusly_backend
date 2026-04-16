package com.campusly.campusly_backend.actors.user.services;

import com.campusly.campusly_backend.actors.user.interfaces.spotted.SpottedCreateRequest;
import com.campusly.campusly_backend.actors.user.interfaces.spotted.SpottedFilters;
import com.campusly.campusly_backend.actors.user.interfaces.spotted.SpottedResponse;
import com.campusly.campusly_backend.actors.user.interfaces.spotted.SpottedUpdateRequest;
import com.campusly.campusly_backend.database.entity.Spotted;
import com.campusly.campusly_backend.database.entity.SpottedImage;
import com.campusly.campusly_backend.database.entity.User;
import com.campusly.campusly_backend.database.repository.SpottedImageRepository;
import com.campusly.campusly_backend.database.repository.SpottedRepository;
import com.campusly.campusly_backend.database.repository.UserRepository;
import com.campusly.campusly_backend.shared.exception.ExceptionBackend;
import com.campusly.campusly_backend.shared.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpottedService {

    private final SpottedRepository spottedRepository;
    private final SpottedImageRepository spottedImageRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    private static final int MAX_IMAGES_PER_SPOTTED = 10;

    // ---------------------------------------------------------------
    // Creazione
    // ---------------------------------------------------------------

    @Transactional
    public SpottedResponse create(SpottedCreateRequest request, List<MultipartFile> images, UUID userId) {
        String errorTitle = "Creazione spotted";
        request.validate(errorTitle);

        User user = findUserOrThrow(userId, errorTitle);

        if (user.getUniversityId() == null) {
            throw ExceptionBackend.fromError(errorTitle,
                    "Devi essere associato a un'università per creare uno spotted. CODICE: SP010",
                    null, HttpStatus.FORBIDDEN);
        }

        Spotted spotted = Spotted.builder()
                .authorId(userId)
                .content(request.content().trim())
                .category(request.category().trim())
                .isAnonymous(request.isAnonymous() != null ? request.isAnonymous() : false)
                .universityId(user.getUniversityId())
                .build();

        spotted = spottedRepository.save(spotted);

        List<SpottedImage> savedImages = persistImages(images, spotted.getId(), userId, 0, errorTitle);

        log.info("Spotted creato — id: {}, userId: {}", spotted.getId(), userId);
        return SpottedResponse.from(spotted, savedImages);
    }

    // ---------------------------------------------------------------
    // Lettura singola
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public SpottedResponse getById(UUID id) {
        String errorTitle = "Recupero spotted";
        Spotted spotted = findSpottedOrThrow(id, errorTitle);
        return SpottedResponse.from(spotted, spotted.getImages());
    }

    // ---------------------------------------------------------------
    // Modifica
    // ---------------------------------------------------------------

    @Transactional
    public SpottedResponse update(UUID id, SpottedUpdateRequest request, List<MultipartFile> newImages, UUID userId) {
        String errorTitle = "Modifica spotted";
        request.validate(errorTitle);

        Spotted spotted = findSpottedOrThrow(id, errorTitle);
        checkOwnership(spotted, userId, errorTitle);

        spotted.setContent(request.content().trim());
        spotted.setCategory(request.category().trim());
        spotted = spottedRepository.save(spotted);

        List<SpottedImage> existingImages = spotted.getImages();

        if (newImages != null && !newImages.isEmpty()) {
            int total = existingImages.size() + newImages.size();
            if (total > MAX_IMAGES_PER_SPOTTED) {
                throw ExceptionBackend.fromError(errorTitle,
                        "Numero massimo di immagini raggiunto (" + MAX_IMAGES_PER_SPOTTED + "). "
                                + "Attualmente presenti: " + existingImages.size() + ". CODICE: SP021",
                        null, HttpStatus.BAD_REQUEST);
            }
            List<SpottedImage> added = persistImages(newImages, spotted.getId(), userId, existingImages.size(), errorTitle);
            existingImages = new ArrayList<>(existingImages);
            existingImages.addAll(added);
        }

        log.info("Spotted aggiornato — id: {}, userId: {}", id, userId);
        return SpottedResponse.from(spotted, existingImages);
    }

    // ---------------------------------------------------------------
    // Eliminazione spotted
    // ---------------------------------------------------------------

    @Transactional
    public void delete(UUID id, UUID userId) {
        String errorTitle = "Eliminazione spotted";
        Spotted spotted = findSpottedOrThrow(id, errorTitle);
        checkOwnership(spotted, userId, errorTitle);

        spotted.getImages().forEach(img -> fileStorageService.deleteFile(img.getFilePath()));

        spottedRepository.delete(spotted);
        log.info("Spotted eliminato — id: {}, userId: {}", id, userId);
    }

    // ---------------------------------------------------------------
    // Eliminazione singola immagine
    // ---------------------------------------------------------------

    @Transactional
    public void deleteImage(UUID spottedId, UUID imageId, UUID userId) {
        String errorTitle = "Eliminazione immagine";
        Spotted spotted = findSpottedOrThrow(spottedId, errorTitle);
        checkOwnership(spotted, userId, errorTitle);

        SpottedImage image = spottedImageRepository.findById(imageId)
                .orElseThrow(() -> ExceptionBackend.fromError(errorTitle,
                        "Immagine non trovata. CODICE: SP030", null, HttpStatus.NOT_FOUND));

        if (!image.getSpottedId().equals(spottedId)) {
            throw ExceptionBackend.fromError(errorTitle,
                    "L'immagine non appartiene a questo spotted. CODICE: SP031",
                    null, HttpStatus.BAD_REQUEST);
        }

        fileStorageService.deleteFile(image.getFilePath());
        spottedImageRepository.delete(image);
        log.info("Immagine eliminata — imageId: {}, spottedId: {}, userId: {}", imageId, spottedId, userId);
    }

    // ---------------------------------------------------------------
    // Lista completa con filtro (no paginazione)
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<SpottedResponse> listAll(SpottedFilters filters) {
        List<Spotted> spottedList;
        if (filters != null && filters.universityId() != null) {
            spottedList = spottedRepository.findByUniversityIdAndStatusOrderByCreatedAtDesc(filters.universityId(), "ACTIVE");
        } else {
            spottedList = spottedRepository.findAll();
        }
        return spottedList.stream().map(s -> SpottedResponse.from(s, s.getImages())).toList();
    }


    // ========================================
    //            Metodi privati
    // ========================================

    private List<SpottedImage> persistImages(List<MultipartFile> files, UUID spottedId,
            UUID userId, int startOrder, String errorTitle) {
        if (files == null || files.isEmpty()) return List.of();

        long nonEmpty = files.stream().filter(f -> f != null && !f.isEmpty()).count();
        if (nonEmpty > MAX_IMAGES_PER_SPOTTED) {
            throw ExceptionBackend.fromError(errorTitle,
                    "Puoi caricare al massimo " + MAX_IMAGES_PER_SPOTTED + " immagini. CODICE: SP020",
                    null, HttpStatus.BAD_REQUEST);
        }

        List<SpottedImage> result = new ArrayList<>();
        int order = startOrder;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            String filePath = fileStorageService.saveFile(file, userId);
            SpottedImage image = SpottedImage.builder()
                    .spottedId(spottedId)
                    .fileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "image")
                    .filePath(filePath)
                    .displayOrder(order++)
                    .build();
            result.add(spottedImageRepository.save(image));
        }
        return result;
    }

    private Spotted findSpottedOrThrow(UUID id, String errorTitle) {
        return spottedRepository.findById(id)
                .orElseThrow(() -> ExceptionBackend.fromError(errorTitle,
                        "Spotted non trovato. CODICE: SP100", null, HttpStatus.NOT_FOUND));
    }

    private User findUserOrThrow(UUID userId, String errorTitle) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ExceptionBackend.fromError(errorTitle,
                        "Utente non trovato. CODICE: SP101", null, HttpStatus.NOT_FOUND));
    }

    private void checkOwnership(Spotted spotted, UUID userId, String errorTitle) {
        if (!userId.equals(spotted.getAuthorId())) {
            throw ExceptionBackend.fromError(errorTitle,
                    "Non sei autorizzato a modificare questo spotted. CODICE: SP102",
                    null, HttpStatus.FORBIDDEN);
        }
    }
}
