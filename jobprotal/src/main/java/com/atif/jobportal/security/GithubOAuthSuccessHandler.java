package com.atif.jobportal.security;

import com.atif.jobportal.service.OAuthLoginCodeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class GithubOAuthSuccessHandler
        implements AuthenticationSuccessHandler {

    private final OAuthLoginCodeService codeService;
    private final String frontendUrl;
    private final String frontendLoginPath;

    public GithubOAuthSuccessHandler(
            OAuthLoginCodeService codeService,
            @Value("${app.frontend-url:${FRONTEND_URL:http://localhost:5500}}")
            String frontendUrl,
            @Value("${app.frontend-login-path:${FRONTEND_LOGIN_PATH:/frontend/login.html}}")
            String frontendLoginPath) {
        this.codeService = codeService;
        this.frontendUrl = frontendUrl;
        this.frontendLoginPath = frontendLoginPath;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {
        if (!(authentication.getPrincipal() instanceof OAuth2User oauthUser)
                || !(oauthUser.getAttribute("localUserId") instanceof Number id)) {
            response.sendRedirect(frontendUrl + frontendLoginPath
                    + "#oauth_error=failed");
            return;
        }

        var session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(frontendUrl + frontendLoginPath
                    + "#oauth_error=failed");
            return;
        }

        String code = codeService.createCode(id.longValue(), session.getId());
        response.sendRedirect(frontendUrl + frontendLoginPath
                + "#oauth_code="
                + URLEncoder.encode(code, StandardCharsets.UTF_8));
    }
}
