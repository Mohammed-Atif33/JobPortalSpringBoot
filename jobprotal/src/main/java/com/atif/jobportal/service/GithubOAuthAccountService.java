package com.atif.jobportal.service;

import com.atif.jobportal.entity.OAuthAccount;
import com.atif.jobportal.entity.User;
import com.atif.jobportal.repository.OAuthAccountRepository;
import com.atif.jobportal.repository.UserRepository;
import com.atif.jobportal.security.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class GithubOAuthAccountService {

    private static final String PROVIDER = "github";

    private final OAuthAccountRepository oauthAccountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public GithubOAuthAccountService(
            OAuthAccountRepository oauthAccountRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.oauthAccountRepository = oauthAccountRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User resolveUser(
            String providerUserId,
            String name,
            String verifiedEmail) {
        return oauthAccountRepository
                .findByProviderAndProviderUserId(PROVIDER, providerUserId)
                .map(OAuthAccount::getUser)
                .orElseGet(() -> createUser(
                        providerUserId,
                        name,
                        verifiedEmail));
    }

    @Transactional(readOnly = true)
    public Optional<User> findExistingGithubUser(String providerUserId) {
        return oauthAccountRepository
                .findByProviderAndProviderUserId(PROVIDER, providerUserId)
                .map(OAuthAccount::getUser);
    }

    private User createUser(
            String providerUserId,
            String name,
            String verifiedEmail) {
        if (userRepository.findByEmailIgnoreCase(verifiedEmail).isPresent()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("github_account_conflict"),
                    "A local account already uses this email address");
        }

        User user = new User(
                name,
                verifiedEmail,
                passwordEncoder.encode(UUID.randomUUID().toString()),
                UserRole.JOB_SEEKER.name());
        User savedUser = userRepository.save(user);
        oauthAccountRepository.save(
                new OAuthAccount(PROVIDER, providerUserId, savedUser));
        return savedUser;
    }
}
