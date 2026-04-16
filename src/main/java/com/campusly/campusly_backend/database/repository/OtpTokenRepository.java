package com.campusly.campusly_backend.database.repository;

import com.campusly.campusly_backend.database.entity.OtpToken;
import com.campusly.campusly_backend.database.entity.OtpTokenType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OtpTokenRepository extends JpaRepository<OtpToken, UUID> {

    Optional<OtpToken> findFirstByUserIdAndTokenTypeAndUsedFalseOrderByCreatedAtDesc(
            UUID userId, OtpTokenType tokenType);

    boolean existsByUserIdAndTokenTypeAndUsedFalse(UUID userId, OtpTokenType tokenType);

    void deleteByUserIdAndTokenType(UUID userId, OtpTokenType tokenType);
}
