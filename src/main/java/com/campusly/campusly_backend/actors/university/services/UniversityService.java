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
    // Pubblica — lista e dettaglio
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<UniversityResponse> getAll() {
        return universityRepository.findAll()
                .stream()
                .map(UniversityResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UniversityResponse getById(UUID id) {
        return UniversityResponse.from(findOrThrow(id, "Recupero università"));
    }

    // ---------------------------------------------------------------
    // Admin — creazione
    // ---------------------------------------------------------------

    @Transactional
    public UniversityResponse create(UniversityRequest request) {
        String errorTitle = "Creazione università";
        request.validate(errorTitle);

        if (request.emailDomain() != null && universityRepository.existsByEmailDomain(request.emailDomain())) {
            throw ExceptionBackend.fromError(errorTitle,
                    "Email domain già associato a un'altra università. CODICE: UN010",
                    null, HttpStatus.CONFLICT);
        }

        University university = University.builder()
                .name(request.name().trim())
                .shortName(request.shortName() != null ? request.shortName().trim() : null)
                .city(request.city().trim())
                .country(request.country().trim())
                .emailDomain(request.emailDomain() != null ? request.emailDomain().trim().toLowerCase() : null)
                .websiteUrl(request.websiteUrl() != null ? request.websiteUrl().trim() : null)
                .international(request.international() != null ? request.international() : false)
                .build();

        university = universityRepository.save(university);
        log.info("Università creata — id: {}, nome: {}", university.getId(), university.getName());
        return UniversityResponse.from(university);
    }

    // ---------------------------------------------------------------
    // Admin — modifica
    // ---------------------------------------------------------------

    @Transactional
    public UniversityResponse update(UUID id, UniversityRequest request) {
        String errorTitle = "Modifica università";
        request.validate(errorTitle);

        University university = findOrThrow(id, errorTitle);

        if (request.emailDomain() != null
                && !request.emailDomain().equalsIgnoreCase(university.getEmailDomain())
                && universityRepository.existsByEmailDomain(request.emailDomain())) {
            throw ExceptionBackend.fromError(errorTitle,
                    "Email domain già associato a un'altra università. CODICE: UN010",
                    null, HttpStatus.CONFLICT);
        }

        university.setName(request.name().trim());
        university.setShortName(request.shortName() != null ? request.shortName().trim() : university.getShortName());
        university.setCity(request.city().trim());
        university.setCountry(request.country().trim());
        university.setEmailDomain(request.emailDomain() != null ? request.emailDomain().trim().toLowerCase() : university.getEmailDomain());
        university.setWebsiteUrl(request.websiteUrl() != null ? request.websiteUrl().trim() : university.getWebsiteUrl());
        university.setInternational(request.international() != null ? request.international() : university.getInternational());

        university = universityRepository.save(university);
        log.info("Università aggiornata — id: {}", id);
        return UniversityResponse.from(university);
    }

    // ---------------------------------------------------------------
    // Admin — eliminazione
    // ---------------------------------------------------------------

    @Transactional
    public void delete(UUID id) {
        String errorTitle = "Eliminazione università";
        University university = findOrThrow(id, errorTitle);
        universityRepository.delete(university);
        log.info("Università eliminata — id: {}, nome: {}", id, university.getName());
    }

    // ========================================
    //            Metodi privati
    // ========================================

    private University findOrThrow(UUID id, String errorTitle) {
        return universityRepository.findById(id)
                .orElseThrow(() -> ExceptionBackend.fromError(errorTitle,
                        "Università non trovata. CODICE: UN100", null, HttpStatus.NOT_FOUND));
    }
}
