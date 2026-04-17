package com.campusly.campusly_backend.actors.university.services;

import com.campusly.campusly_backend.actors.university.interfaces.UniversityRequest;
import com.campusly.campusly_backend.actors.university.interfaces.UniversityResponse;
import com.campusly.campusly_backend.database.entity.University;
import com.campusly.campusly_backend.database.repository.UniversityRepository;
import com.campusly.campusly_backend.shared.exception.ExceptionBackend;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UniversityService {

    private final UniversityRepository universityRepository;


    // ---------------------------------------------------------------
    // lista università
    // ---------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<UniversityResponse> getAll() {
        return universityRepository.findAll()
                .stream()
                .map(UniversityResponse::from)
                .toList();
    }


    // ---------------------------------------------------------------
    // dettaglio università per ID
    // ---------------------------------------------------------------
    @Transactional(readOnly = true)
    public UniversityResponse getById(UUID id) {
        return UniversityResponse.from(findOrThrow(id, "Recupero università"));
    }


    // ---------------------------------------------------------------
    // ! UTENTE ADMIN — creazione nuova università
    // ---------------------------------------------------------------
    @Transactional
    public UniversityResponse create(UniversityRequest request) {
        String errorTitle = "Creazione università";
        request.isValid(errorTitle);

        if (request.getEmailDomain() != null && universityRepository.existsByEmailDomain(request.getEmailDomain())) {
            throw ExceptionBackend.fromError(errorTitle,
                    "Email domain già associato a un'altra università. CODICE: UN010",
                    null, HttpStatus.CONFLICT);
        }

        University university = University.builder()
                .name(request.getName().trim())
                .shortName(request.getShortName() != null ? request.getShortName().trim() : null)
                .city(request.getCity().trim())
                .country(request.getCountry().trim())
                .emailDomain(request.getEmailDomain() != null ? request.getEmailDomain().trim().toLowerCase() : null)
                .websiteUrl(request.getWebsiteUrl() != null ? request.getWebsiteUrl().trim() : null)
                .international(request.getInternational() != null ? request.getInternational() : false)
                .build();

        university = universityRepository.save(university);
        log.info("Università creata — id: {}, nome: {}", university.getId(), university.getName());
        return UniversityResponse.from(university);
    }


    // ---------------------------------------------------------------
    // ! UTENTE ADMIN — modifica università
    // ---------------------------------------------------------------
    @Transactional
    public UniversityResponse update(UUID id, UniversityRequest request) {
        String errorTitle = "Modifica università";
        request.isValid(errorTitle);

        University university = findOrThrow(id, errorTitle);

        if (request.getEmailDomain() != null
                && !request.getEmailDomain().equalsIgnoreCase(university.getEmailDomain())
                && universityRepository.existsByEmailDomain(request.getEmailDomain())) {
            throw ExceptionBackend.fromError(errorTitle,
                    "Email domain già associato a un'altra università. CODICE: UN010",
                    null, HttpStatus.CONFLICT);
        }

        university.setName(request.getName().trim());
        university.setShortName(request.getShortName() != null ? request.getShortName().trim() : university.getShortName());
        university.setCity(request.getCity().trim());
        university.setCountry(request.getCountry().trim());
        university.setEmailDomain(request.getEmailDomain() != null ? request.getEmailDomain().trim().toLowerCase() : university.getEmailDomain());
        university.setWebsiteUrl(request.getWebsiteUrl() != null ? request.getWebsiteUrl().trim() : university.getWebsiteUrl());
        university.setInternational(request.getInternational() != null ? request.getInternational() : university.getInternational());

        university = universityRepository.save(university);
        log.info("Università aggiornata — id: {}", id);
        return UniversityResponse.from(university);
    }


    // ---------------------------------------------------------------
    // ! UTENTE ADMIN — eliminazione università
    // ---------------------------------------------------------------
    @Transactional
    public void delete(UUID id) {
        String errorTitle = "Eliminazione università";
        University university = findOrThrow(id, errorTitle);
        universityRepository.delete(university);
        log.info("Università eliminata — id: {}, nome: {}", id, university.getName());
    }

    
    // ========================================
    // *            Metodi privati
    // ========================================

    // Trova università per ID o lancia eccezione con messaggio e codice specifici
    private University findOrThrow(UUID id, String errorTitle) {
        return universityRepository.findById(id)
                .orElseThrow(() -> ExceptionBackend.fromError(errorTitle,
                        "Università non trovata. CODICE: UN100", null, HttpStatus.NOT_FOUND));
    }
}
