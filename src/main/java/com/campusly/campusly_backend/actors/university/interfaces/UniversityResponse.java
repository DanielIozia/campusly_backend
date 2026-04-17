package com.campusly.campusly_backend.actors.university.interfaces;

import com.campusly.campusly_backend.database.entity.University;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UniversityResponse {
    private UUID id;
    private String name;
    private String shortName;
    private String city;
    private String country;
    private String emailDomain;
    private String websiteUrl;
    private String logoUrl;
    private Boolean international;

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
