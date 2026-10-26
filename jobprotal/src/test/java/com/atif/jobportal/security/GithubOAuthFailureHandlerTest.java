package com.atif.jobportal.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GithubOAuthFailureHandlerTest {

    @Test
    void returnsOnlyKnownFailureCodeToConfiguredFrontend()
            throws ServletException, IOException {
        GithubOAuthFailureHandler handler =
                new GithubOAuthFailureHandler(
                        "http://localhost:5500",
                        "/frontend/login.html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(
                new MockHttpServletRequest(),
                response,
                new OAuth2AuthenticationException(
                        new OAuth2Error("access_denied"),
                        "Provider supplied sensitive detail"));

        assertEquals(
                "http://localhost:5500/frontend/login.html#oauth_error=denied",
                response.getRedirectedUrl());
    }
}
