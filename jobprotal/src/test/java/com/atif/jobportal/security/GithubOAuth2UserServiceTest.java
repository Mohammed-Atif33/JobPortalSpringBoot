package com.atif.jobportal.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GithubOAuth2UserServiceTest {

    @Test
    void selectsVerifiedEmailAndPrefersPrimary() {
        String email = GithubOAuth2UserService.selectVerifiedEmail(List.of(
                Map.of(
                        "email", "other@example.com",
                        "verified", true,
                        "primary", false),
                Map.of(
                        "email", "primary@example.com",
                        "verified", true,
                        "primary", true)));

        assertEquals("primary@example.com", email);
    }

    @Test
    void ignoresPublicButUnverifiedEmail() {
        OAuth2AuthenticationException exception = assertThrows(
                OAuth2AuthenticationException.class,
                () -> GithubOAuth2UserService.selectVerifiedEmail(List.of(
                        Map.of(
                                "email", "public@example.com",
                                "verified", false,
                                "primary", true))));

        assertEquals(
                "github_email_unavailable",
                exception.getError().getErrorCode());
    }
}
