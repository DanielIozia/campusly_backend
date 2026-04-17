package com.campusly.campusly_backend.actors.user.interfaces.spotted;
import com.campusly.campusly_backend.database.entity.SpottedImage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpottedImageResponse {
    private UUID id;
    private String url;
    private Integer displayOrder;

    public static SpottedImageResponse from(SpottedImage image) {
        return new SpottedImageResponse(
                image.getId(),
                "/files/" + image.getFilePath(),
                image.getDisplayOrder()
        );
    }
}
