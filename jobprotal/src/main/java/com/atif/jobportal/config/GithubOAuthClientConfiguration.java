package com.atif.jobportal.config;

import com.atif.jobportal.security.GithubOAuthClientConfiguredCondition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
public class GithubOAuthClientConfiguration {

    @Bean
    @Conditional(GithubOAuthClientConfiguredCondition.class)
    public ClientRegistrationRepository githubClientRegistrationRepository(
            @Value("${app.github.client-id:${GITHUB_CLIENT_ID:}}")
            String clientId,
            @Value("${app.github.client-secret:${GITHUB_CLIENT_SECRET:}}")
            String clientSecret) {
        ClientRegistration registration = ClientRegistration
                .withRegistrationId("github")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(
                        ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(
                        AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("read:user", "user:email")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri("https://api.github.com/user")
                .userNameAttributeName("id")
                .clientName("GitHub")
                .build();

        return new InMemoryClientRegistrationRepository(registration);
    }
}
