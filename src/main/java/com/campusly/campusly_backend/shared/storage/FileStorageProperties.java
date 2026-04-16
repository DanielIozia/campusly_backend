package com.campusly.campusly_backend.shared.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.storage")
@Getter
@Setter
public class FileStorageProperties {

    private String uploadDir = "uploads";
    private long maxFileSize = 5_242_880L; // 5 MB
    private List<String> allowedTypes = List.of("image/jpeg", "image/png", "image/webp");
}
