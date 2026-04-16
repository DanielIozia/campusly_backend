package com.campusly.campusly_backend.actors.user.interfaces.user;

import com.campusly.campusly_backend.database.entity.University;
import com.campusly.campusly_backend.database.entity.User;

import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String username,
        String firstName,
        String lastName,
        String email,
        UUID universityId,
        String universityName,
        String universityCity,
        Boolean isErasmus,
        UUID erasmusUnivId,
        String photoUrl,
        String bio
) {
    public static UserProfileResponse from(User user, University university) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUniversityId(),
                university != null ? university.getName() : null,
                university != null ? university.getCity() : null,
                user.getIsErasmus(),
                user.getErasmusUnivId(),
                user.getPhotoUrl(),
                user.getBio()
        );
    }
}
