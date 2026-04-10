package com.campusly.campusly_backend.database.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.campusly.campusly_backend.actors.user.interfaces.registration.enums.UserStatus;
import com.campusly.campusly_backend.auth.entity.AuthProvider;
import com.campusly.campusly_backend.auth.entity.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entità principale degli utenti Campusly.
 *
 * Il campo status gestisce la macchina a stati della registrazione locale:
 *   CODE_VERIFICATION → SIGNUP → ACTIVE
 * Gli utenti Google vengono creati direttamente in stato ACTIVE.
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

    /** Nullable: viene valorizzato solo al completamento del profilo (step 3). */
    @Column(name = "username", unique = true, length = 50)
    private String username;

    /** Nullable: viene valorizzato solo al completamento del profilo (step 3). */
    @Column(name = "first_name", length = 100)
    private String firstName;

    /** Nullable: viene valorizzato solo al completamento del profilo (step 3). */
    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    /** Nullable: viene valorizzato solo al completamento del profilo (step 3). */
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    /** Nullable: viene valorizzata solo al completamento del profilo (step 3). */
    @Column(name = "birth_date")
    private LocalDate birthDate;

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

    /**
     * Stato della registrazione.
     * Gli utenti Google saltano direttamente ad ACTIVE.
     * Gli utenti LOCAL passano per CODE_VERIFICATION → SIGNUP → ACTIVE.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    /** Codice OTP a 6 cifre. Viene azzerato dopo la verifica. */
    @Column(name = "otp_code", length = 10)
    private String otpCode;

    /** Timestamp di scadenza del codice OTP (10 minuti dalla generazione). */
    @Column(name = "otp_expires_at")
    private LocalDateTime otpExpiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
