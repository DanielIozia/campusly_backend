package com.campusly.campusly_backend.database.repository;

import com.campusly.campusly_backend.database.entity.SpottedImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpottedImageRepository extends JpaRepository<SpottedImage, UUID> {
}
