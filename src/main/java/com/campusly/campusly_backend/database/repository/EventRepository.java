package com.campusly.campusly_backend.database.repository;

import com.campusly.campusly_backend.database.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    List<Event> findByUniversityIdAndStatusOrderByEventDateAsc(UUID universityId, String status);

    List<Event> findByOrganizerIdOrderByEventDateDesc(UUID organizerId);
}
