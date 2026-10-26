package com.atif.jobportal.repository;

import com.atif.jobportal.entity.OAuthLoginCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface OAuthLoginCodeRepository
        extends JpaRepository<OAuthLoginCode, Long> {

    @Modifying
    @Query("""
            update OAuthLoginCode code
            set code.consumedAt = :now
            where code.codeHash = :codeHash
              and code.sessionId = :sessionId
              and code.expiresAt > :now
              and code.consumedAt is null
            """)
    int consumeIfValid(
            @Param("codeHash") String codeHash,
            @Param("sessionId") String sessionId,
            @Param("now") Instant now);

    Optional<OAuthLoginCode> findByCodeHashAndSessionId(
            String codeHash,
            String sessionId);

    @Modifying
    @Query("delete from OAuthLoginCode code where code.expiresAt <= :now")
    int deleteExpired(@Param("now") Instant now);
}
