package com.campusly.campusly_backend.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entità principale degli utenti Campusly.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "phone", length = 25)
    private String phone;

    @Column(name = "university_id")
    private UUID universityId;

    @Column(name = "erasmus_univ_id")
    private UUID erasmusUnivId;

    @Builder.Default
    @Column(name = "is_erasmus")
    private Boolean isErasmus = false;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "bio", length = 300)
    private String bio;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", length = 20)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    private Role role = Role.CAMPUSLY_USER;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
