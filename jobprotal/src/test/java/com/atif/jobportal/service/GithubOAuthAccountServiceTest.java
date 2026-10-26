package com.atif.jobportal.service;

import com.atif.jobportal.entity.OAuthAccount;
import com.atif.jobportal.entity.User;
import com.atif.jobportal.repository.OAuthAccountRepository;
import com.atif.jobportal.repository.UserRepository;
import com.atif.jobportal.security.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GithubOAuthAccountServiceTest {

    @Mock
    private OAuthAccountRepository oauthAccountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private GithubOAuthAccountService accountService;

    @Test
    void repeatedProviderIdentityResolvesToExistingLocalUser() {
        User existingUser = new User(
                "Recruiter",
                "recruiter@example.com",
                "encoded",
                UserRole.RECRUITER.name());
        OAuthAccount account = new OAuthAccount(
                "github",
                "stable-provider-id",
                existingUser);
        when(oauthAccountRepository.findByProviderAndProviderUserId(
                "github",
                "stable-provider-id"))
                .thenReturn(Optional.of(account));

        User result = accountService.resolveUser(
                "stable-provider-id",
                "New GitHub Display Name",
                null);

        assertSame(existingUser, result);
        assertEquals(UserRole.RECRUITER.name(), result.getRole());
        verify(userRepository, never()).save(any(User.class));
        verify(oauthAccountRepository, never()).save(any(OAuthAccount.class));
    }

    @Test
    void newGithubUserAlwaysGetsJobSeekerRole() {
        when(oauthAccountRepository.findByProviderAndProviderUserId(
                "github",
                "new-provider-id"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("new@example.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class)))
                .thenReturn("encoded-random-password");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = accountService.resolveUser(
                "new-provider-id",
                "New User",
                "new@example.com");

        assertEquals(UserRole.JOB_SEEKER.name(), result.getRole());
        verify(oauthAccountRepository).save(any(OAuthAccount.class));
    }

    @Test
    void doesNotLinkToExistingAccountByEmail() {
        when(oauthAccountRepository.findByProviderAndProviderUserId(
                "github",
                "unlinked-provider-id"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("existing@example.com"))
                .thenReturn(Optional.of(new User()));

        OAuth2AuthenticationException exception = assertThrows(
                OAuth2AuthenticationException.class,
                () -> accountService.resolveUser(
                        "unlinked-provider-id",
                        "Existing User",
                        "existing@example.com"));

        assertEquals(
                "github_account_conflict",
                exception.getError().getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }
}
