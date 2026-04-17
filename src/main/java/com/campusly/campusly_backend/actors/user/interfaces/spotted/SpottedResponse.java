package com.campusly.campusly_backend.actors.user.interfaces.spotted;

import com.campusly.campusly_backend.database.entity.Spotted;
import com.campusly.campusly_backend.database.entity.SpottedImage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpottedResponse {
    private UUID id;
    private UUID authorId;
    private String content;
    private String category;
    private Boolean isAnonymous;
    private Integer likeCount;
    private Integer commentCount;
    private String status;
    private List<SpottedImageResponse> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


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
