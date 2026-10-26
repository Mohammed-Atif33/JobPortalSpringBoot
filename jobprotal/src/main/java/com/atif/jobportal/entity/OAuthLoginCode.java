package com.atif.jobportal.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "oauth_login_codes",
        indexes = @Index(
                name = "idx_oauth_code_expiry",
                columnList = "expires_at"
        )
)
public class OAuthLoginCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_hash", nullable = false, unique = true, length = 64)
    private String codeHash;

    @Column(name = "session_id", nullable = false, length = 128)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    protected OAuthLoginCode() {
    }

    public OAuthLoginCode(
            String codeHash,
            String sessionId,
            User user,
            Instant expiresAt) {
        this.codeHash = codeHash;
        this.sessionId = sessionId;
        this.user = user;
        this.expiresAt = expiresAt;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public String getSessionId() {
        return sessionId;
    }

    public User getUser() {
        return user;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getConsumedAt() {
        return consumedAt;
    }
}
