package com.atif.jobportal.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GithubOAuthClientConfigurationTest {

    @Test
    void buildsGithubRegistrationWithExpectedCallbackAndEmailScope() {
        var registrations = new GithubOAuthClientConfiguration()
                .githubClientRegistrationRepository(
                        "test-client-id",
                        "test-client-secret");
        var github = registrations.findByRegistrationId("github");

        assertEquals("test-client-id", github.getClientId());
        assertEquals("https://github.com/login/oauth/authorize",
                github.getProviderDetails().getAuthorizationUri());
        assertEquals(AuthorizationGrantType.AUTHORIZATION_CODE,
                github.getAuthorizationGrantType());
        assertEquals(
                "{baseUrl}/login/oauth2/code/{registrationId}",
                github.getRedirectUri());
        assertTrue(github.getScopes().contains("user:email"));
    }
}
