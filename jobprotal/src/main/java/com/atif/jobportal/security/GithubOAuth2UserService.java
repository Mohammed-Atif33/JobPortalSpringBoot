package com.atif.jobportal.security;

import com.atif.jobportal.entity.User;
import com.atif.jobportal.service.GithubOAuthAccountService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GithubOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final ParameterizedTypeReference<List<Map<String, Object>>>
            EMAIL_RESPONSE_TYPE = new ParameterizedTypeReference<>() { };

    private final DefaultOAuth2UserService delegate =
            new DefaultOAuth2UserService();
    private final RestClient restClient;
    private final GithubOAuthAccountService accountService;

    public GithubOAuth2UserService(
            GithubOAuthAccountService accountService) {
        this.restClient = RestClient.create();
        this.accountService = accountService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {
        OAuth2User githubUser = delegate.loadUser(userRequest);
        Map<String, Object> attributes = githubUser.getAttributes();

        Object providerId = attributes.get("id");
        if (providerId == null) {
            throw oauthError(
                    "github_profile_invalid",
                    "GitHub did not return a stable account identifier");
        }

        String login = stringAttribute(attributes, "login");
        String name = stringAttribute(attributes, "name");
        if (!StringUtils.hasText(name)) {
            name = login;
        }
        if (!StringUtils.hasText(name)) {
            throw oauthError(
                    "github_profile_invalid",
                    "GitHub did not return an account name");
        }

        String displayName = name;
        String githubId = String.valueOf(providerId);
        User localUser = accountService.findExistingGithubUser(githubId)
                .orElseGet(() -> accountService.resolveUser(
                        githubId,
                        displayName,
                        loadVerifiedEmail(userRequest)));

        Map<String, Object> localAttributes = new HashMap<>(attributes);
        localAttributes.put("localUserId", localUser.getId());
        return new DefaultOAuth2User(
                githubUser.getAuthorities(),
                localAttributes,
                "id");
    }

    private String loadVerifiedEmail(OAuth2UserRequest userRequest) {
        List<Map<String, Object>> emails;
        try {
            emails = restClient.get()
                    .uri("https://api.github.com/user/emails")
                    .headers(headers -> headers.setBearerAuth(
                            userRequest.getAccessToken().getTokenValue()))
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "CareerNest")
                    .retrieve()
                    .body(EMAIL_RESPONSE_TYPE);
        } catch (RestClientException exception) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("github_email_lookup_failed"),
                    "Unable to retrieve a verified GitHub email address",
                    exception);
        }

        return selectVerifiedEmail(emails);
    }

    static String selectVerifiedEmail(List<Map<String, Object>> emails) {
        if (emails != null) {
            String verifiedEmail = null;
            for (Map<String, Object> entry : emails) {
                Object email = entry.get("email");
                if (!(email instanceof String address)
                        || !StringUtils.hasText(address)
                        || !Boolean.TRUE.equals(entry.get("verified"))) {
                    continue;
                }
                if (Boolean.TRUE.equals(entry.get("primary"))) {
                    return address;
                }
                if (verifiedEmail == null) {
                    verifiedEmail = address;
                }
            }
            if (verifiedEmail != null) {
                return verifiedEmail;
            }
        }

        throw new OAuth2AuthenticationException(
                new OAuth2Error("github_email_unavailable"),
                "No verified email address is available from GitHub");
    }

    private String stringAttribute(
            Map<String, Object> attributes,
            String key) {
        Object value = attributes.get(key);
        return value instanceof String string ? string : null;
    }

    private OAuth2AuthenticationException oauthError(
            String errorCode,
            String description) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(errorCode),
                description);
    }
}
