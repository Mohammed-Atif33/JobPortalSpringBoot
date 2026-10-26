package com.atif.jobportal.security;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

public class GithubOAuthClientConfiguredCondition implements Condition {

    @Override
    public boolean matches(
            ConditionContext context,
            AnnotatedTypeMetadata metadata) {
        String clientId = context.getEnvironment().getProperty(
                "app.github.client-id",
                context.getEnvironment().getProperty("GITHUB_CLIENT_ID"));
        String clientSecret = context.getEnvironment().getProperty(
                "app.github.client-secret",
                context.getEnvironment().getProperty("GITHUB_CLIENT_SECRET"));
        return StringUtils.hasText(clientId)
                && StringUtils.hasText(clientSecret);
    }
}
