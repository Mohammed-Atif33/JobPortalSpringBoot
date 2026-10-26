package com.atif.jobportal.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GithubOAuthFailureHandler
        implements AuthenticationFailureHandler {

    private final String frontendUrl;
    private final String frontendLoginPath;

    public GithubOAuthFailureHandler(
            @Value("${app.frontend-url:${FRONTEND_URL:http://localhost:5500}}")
            String frontendUrl,
            @Value("${app.frontend-login-path:${FRONTEND_LOGIN_PATH:/frontend/login.html}}")
            String frontendLoginPath) {
        this.frontendUrl = frontendUrl;
        this.frontendLoginPath = frontendLoginPath;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {
        String error = "failed";
        if (exception instanceof OAuth2AuthenticationException oauthException) {
            error = switch (oauthException.getError().getErrorCode()) {
                case "access_denied" -> "denied";
                case "github_email_unavailable" -> "missing_email";
                case "github_email_lookup_failed" -> "email_lookup_failed";
                case "github_account_conflict" -> "account_conflict";
                default -> "failed";
            };
        }
        response.sendRedirect(frontendUrl + frontendLoginPath
                + "#oauth_error="
                + error);
    }
}
