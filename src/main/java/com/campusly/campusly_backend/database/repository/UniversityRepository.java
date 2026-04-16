package com.campusly.campusly_backend.database.repository;

import com.campusly.campusly_backend.database.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UniversityRepository extends JpaRepository<University, UUID> {

    Optional<University> findByEmailDomain(String emailDomain);

    boolean existsByEmailDomain(String emailDomain);
}
