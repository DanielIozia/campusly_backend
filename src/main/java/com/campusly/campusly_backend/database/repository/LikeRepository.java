package com.campusly.campusly_backend.database.repository;

import com.campusly.campusly_backend.database.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LikeRepository extends JpaRepository<Like, UUID> {

    Optional<Like> findByUserIdAndTargetTypeAndTargetId(UUID userId, String targetType, UUID targetId);

    boolean existsByUserIdAndTargetTypeAndTargetId(UUID userId, String targetType, UUID targetId);

    long countByTargetTypeAndTargetId(String targetType, UUID targetId);
}
