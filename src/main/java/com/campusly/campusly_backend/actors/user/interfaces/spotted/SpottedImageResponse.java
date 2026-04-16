package com.campusly.campusly_backend.actors.user.interfaces.spotted;

import com.campusly.campusly_backend.database.entity.SpottedImage;

import java.util.UUID;

public record SpottedImageResponse(
        UUID id,
        String url,
        Integer displayOrder
) {
    public static SpottedImageResponse from(SpottedImage image) {
        return new SpottedImageResponse(
                image.getId(),
                "/files/" + image.getFilePath(),
                image.getDisplayOrder()
        );
    }
}
