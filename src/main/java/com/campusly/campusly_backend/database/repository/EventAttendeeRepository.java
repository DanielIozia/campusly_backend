package com.campusly.campusly_backend.database.repository;

import com.campusly.campusly_backend.database.entity.EventAttendee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventAttendeeRepository extends JpaRepository<EventAttendee, UUID> {

    Optional<EventAttendee> findByEventIdAndUserId(UUID eventId, UUID userId);

    boolean existsByEventIdAndUserId(UUID eventId, UUID userId);

    List<EventAttendee> findByEventId(UUID eventId);

    List<EventAttendee> findByUserId(UUID userId);
}
