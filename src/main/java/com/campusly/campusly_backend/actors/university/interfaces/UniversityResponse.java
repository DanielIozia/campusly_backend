package com.campusly.campusly_backend.actors.university.interfaces;

import com.campusly.campusly_backend.database.entity.University;

import java.util.UUID;

public record UniversityResponse(
        UUID id,
        String name,
        String shortName,
        String city,
        String country,
        String emailDomain,
        String websiteUrl,
        String logoUrl,
        Boolean international
) {
    public static UniversityResponse from(University university) {
        return new UniversityResponse(
                university.getId(),
                university.getName(),
                university.getShortName(),
                university.getCity(),
                university.getCountry(),
                university.getEmailDomain(),
                university.getWebsiteUrl(),
                university.getLogoUrl(),
                university.getInternational()
        );
    }
}
