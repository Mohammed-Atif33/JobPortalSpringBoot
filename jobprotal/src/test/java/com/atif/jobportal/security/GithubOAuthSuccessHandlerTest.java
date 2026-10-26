package com.atif.jobportal.security;

import com.atif.jobportal.service.OAuthLoginCodeService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GithubOAuthSuccessHandlerTest {

    @Test
    void redirectsToConfiguredLoginPageWithOneTimeCodeInFragment()
            throws ServletException, IOException {
        OAuthLoginCodeService codeService = mock(OAuthLoginCodeService.class);
        GithubOAuthSuccessHandler handler = new GithubOAuthSuccessHandler(
                codeService,
                "http://localhost:5500",
                "/frontend/login.html");
        OAuth2User oauthUser = mock(OAuth2User.class);
        when(oauthUser.getAttribute("localUserId")).thenReturn(17L);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(oauthUser);
        MockHttpServletRequest request = new MockHttpServletRequest();
        String sessionId = request.getSession(true).getId();
        when(codeService.createCode(17L, sessionId)).thenReturn("one-time-code");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication);

        assertEquals(
                "http://localhost:5500/frontend/login.html#oauth_code=one-time-code",
                response.getRedirectedUrl());
        verify(codeService).createCode(17L, sessionId);
    }
}
