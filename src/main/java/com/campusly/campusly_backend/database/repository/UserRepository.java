package com.campusly.campusly_backend.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusly.campusly_backend.database.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
