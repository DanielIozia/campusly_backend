package com.campusly.campusly_backend.actors.user.interfaces.user;

import com.campusly.campusly_backend.database.entity.University;
import com.campusly.campusly_backend.database.entity.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private UniversityProfile universityProfile;
    private Boolean isErasmus;
    private UUID erasmusUnivId;
    private String photoUrl;
    private String bio;

    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UniversityProfile {
        private UUID id;
        private String name;
        private String city;
    }

    public static UserProfileResponse from(User user, University university) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                university == null ? null : new UniversityProfile(university.getId(), university.getName(), university.getCity()),
                user.getIsErasmus(),
                user.getErasmusUnivId(),
                user.getPhotoUrl(),
                user.getBio());
    }
}
