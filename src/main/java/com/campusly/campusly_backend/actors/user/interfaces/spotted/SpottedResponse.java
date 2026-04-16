package com.campusly.campusly_backend.actors.user.interfaces.spotted;

import com.campusly.campusly_backend.database.entity.Spotted;
import com.campusly.campusly_backend.database.entity.SpottedImage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SpottedResponse(
        UUID id,
        UUID authorId,
        String content,
        String category,
        Boolean isAnonymous,
        Integer likeCount,
        Integer commentCount,
        String status,
        List<SpottedImageResponse> images,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SpottedResponse from(Spotted spotted, List<SpottedImage> images) {
        return new SpottedResponse(
                spotted.getId(),
                spotted.getAuthorId(),
                spotted.getContent(),
                spotted.getCategory(),
                spotted.getIsAnonymous(),
                spotted.getLikeCount(),
                spotted.getCommentCount(),
                spotted.getStatus(),
                images.stream().map(SpottedImageResponse::from).toList(),
                spotted.getCreatedAt(),
                spotted.getUpdatedAt()
        );
    }
}
