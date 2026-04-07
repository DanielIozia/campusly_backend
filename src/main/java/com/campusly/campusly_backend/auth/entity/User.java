package com.campusly.campusly_backend.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entity JPA mappata sulla tabella {@code users}.
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

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 100)
    private String cognome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "data_nascita")
    private LocalDate dataNascita;

    @Column(length = 20)
    private String telefono;

    @Column(name = "university_id")
    private UUID universityId;

    @Column(name = "erasmus_univ_id")
    private UUID erasmusUnivId;

    @Column(name = "is_erasmus")
    @Builder.Default
    private Boolean isErasmus = false;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(length = 300)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", length = 20)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Role role = Role.STUDENT;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
