package com.campusly.campusly_backend.actors.user.services;

import com.campusly.campusly_backend.actors.user.interfaces.user.UserProfileResponse;
import com.campusly.campusly_backend.actors.user.interfaces.user.UserUniversityRequest;
import com.campusly.campusly_backend.database.entity.University;
import com.campusly.campusly_backend.database.entity.User;
import com.campusly.campusly_backend.database.repository.UniversityRepository;
import com.campusly.campusly_backend.database.repository.UserRepository;
import com.campusly.campusly_backend.shared.exception.ExceptionBackend;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;

    // ---------------------------------------------------------------
    // Associa / modifica università dell'utente autenticato
    // ---------------------------------------------------------------
    @Transactional
    public UserProfileResponse updateUniversity(UserUniversityRequest request, UUID userId) {
        String errorTitle = "Aggiornamento università";
        request.isValid(errorTitle);

        User user = findUserOrThrow(userId, errorTitle);

        University university = universityRepository.findById(request.getUniversityId())
                .orElseThrow(() -> ExceptionBackend.fromError(errorTitle,
                        "Università non trovata. CODICE: US010", null, HttpStatus.NOT_FOUND));

        user.setUniversityId(university.getId());
        userRepository.save(user);

        return UserProfileResponse.from(user, university);
    }

    // ========================================
    //            Metodi privati
    // ========================================

    // Trova utente per ID o lancia eccezione con messaggio e codice specifici
    private User findUserOrThrow(UUID userId, String errorTitle) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ExceptionBackend.fromError(errorTitle,
                        "Utente non trovato. CODICE: US100", null, HttpStatus.NOT_FOUND));
    }
}
