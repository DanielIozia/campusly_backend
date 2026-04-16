package com.campusly.campusly_backend.database.repository;

import com.campusly.campusly_backend.database.entity.Spotted;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpottedRepository extends JpaRepository<Spotted, UUID> {

    List<Spotted> findByUniversityIdAndStatusOrderByCreatedAtDesc(UUID universityId, String status);

    List<Spotted> findByAuthorIdOrderByCreatedAtDesc(UUID authorId);

    Page<Spotted> findByUniversityIdAndStatusOrderByCreatedAtDesc(UUID universityId, String status, Pageable pageable);

    Page<Spotted> findByUniversityIdAndStatusAndCategoryOrderByCreatedAtDesc(UUID universityId, String status, String category, Pageable pageable);
}
