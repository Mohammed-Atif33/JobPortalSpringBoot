package com.atif.jobportal.service;

import com.atif.jobportal.entity.OAuthLoginCode;
import com.atif.jobportal.entity.User;
import com.atif.jobportal.repository.OAuthLoginCodeRepository;
import com.atif.jobportal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class OAuthLoginCodeService {

    private static final Duration CODE_LIFETIME = Duration.ofMinutes(2);

    private final OAuthLoginCodeRepository codeRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public OAuthLoginCodeService(
            OAuthLoginCodeRepository codeRepository,
            UserRepository userRepository) {
        this.codeRepository = codeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public String createCode(Long userId, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalStateException(
                    "An OAuth browser session is required");
        }

        Instant now = Instant.now();
        codeRepository.deleteExpired(now);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated local user no longer exists"));

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String code = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
        codeRepository.save(new OAuthLoginCode(
                hashCode(code),
                sessionId,
                user,
                now.plus(CODE_LIFETIME)));
        return code;
    }

    @Transactional
    public User consumeCode(String code, String sessionId) {
        Instant now = Instant.now();
        String codeHash = hashCode(code);
        int consumed = codeRepository.consumeIfValid(
                codeHash,
                sessionId,
                now);
        if (consumed != 1) {
            throw new InvalidOAuthLoginCodeException();
        }

        return codeRepository.findByCodeHashAndSessionId(codeHash, sessionId)
                .map(OAuthLoginCode::getUser)
                .orElseThrow(InvalidOAuthLoginCodeException::new);
    }

    private String hashCode(String code) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(code.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable",
                    exception);
        }
    }
}
