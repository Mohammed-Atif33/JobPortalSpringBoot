package com.atif.jobportal.controller;

import com.atif.jobportal.dto.LoginResponseDTO;
import com.atif.jobportal.dto.OAuthCodeExchangeRequest;
import com.atif.jobportal.entity.User;
import com.atif.jobportal.security.UserRole;
import com.atif.jobportal.service.InvalidOAuthLoginCodeException;
import com.atif.jobportal.service.OAuthLoginCodeService;
import com.atif.jobportal.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class GithubOAuthController {

    private final ObjectProvider<ClientRegistrationRepository>
            clientRegistrationRepository;
    private final OAuthLoginCodeService codeService;
    private final JwtService jwtService;
    private final String frontendUrl;
    private final String frontendLoginPath;

    public GithubOAuthController(
            ObjectProvider<ClientRegistrationRepository>
                    clientRegistrationRepository,
            OAuthLoginCodeService codeService,
            JwtService jwtService,
            @Value("${app.frontend-url:${FRONTEND_URL:http://localhost:5500}}")
            String frontendUrl,
            @Value("${app.frontend-login-path:${FRONTEND_LOGIN_PATH:/frontend/login.html}}")
            String frontendLoginPath) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.codeService = codeService;
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
        this.frontendLoginPath = frontendLoginPath;
    }

    @org.springframework.web.bind.annotation.GetMapping("/github")
    public ResponseEntity<Void> startGithubLogin(HttpServletRequest request) {
        ClientRegistrationRepository registrations =
                clientRegistrationRepository.getIfAvailable();
        if (registrations == null
                || registrations.findByRegistrationId("github") == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", frontendUrl
                            + frontendLoginPath
                            + "#oauth_error=not_configured")
                    .build();
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", request.getContextPath()
                        + "/oauth2/authorization/github")
                .build();
    }

    @PostMapping(
            path = "/oauth/exchange",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponseDTO> exchangeCode(
            @Valid @RequestBody OAuthCodeExchangeRequest request,
            HttpServletRequest servletRequest) {
        var session = servletRequest.getSession(false);
        if (session == null) {
            throw new InvalidOAuthLoginCodeException();
        }

        User user = codeService.consumeCode(request.getCode(), session.getId());
        UserRole role;
        try {
            role = UserRole.valueOf(user.getRole());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User does not have a supported role");
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(role.name())
                .build();
        String token = jwtService.generateToken(userDetails);

        session.invalidate();
        return ResponseEntity.ok(new LoginResponseDTO(token, role.name()));
    }
}
