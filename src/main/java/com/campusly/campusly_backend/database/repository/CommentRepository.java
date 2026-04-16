package com.campusly.campusly_backend.database.repository;

import com.campusly.campusly_backend.database.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findBySpottedIdAndStatusOrderByCreatedAtAsc(UUID spottedId, String status);
}
