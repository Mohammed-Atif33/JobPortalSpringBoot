package com.atif.jobportal.config;

import com.atif.jobportal.security.CustomUserDetailsService;
import com.atif.jobportal.security.GithubOAuth2UserService;
import com.atif.jobportal.security.GithubOAuthFailureHandler;
import com.atif.jobportal.security.GithubOAuthSuccessHandler;
import com.atif.jobportal.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.ArrayList;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

        @Bean  
        public SecurityFilterChain securityFilterChain(
                HttpSecurity http,
                AuthenticationProvider authenticationProvider,
                JwtAuthenticationFilter jwtAuthenticationFilter,
                CorsConfigurationSource corsConfigurationSource,
                org.springframework.beans.factory.ObjectProvider<
                        ClientRegistrationRepository> clientRegistrations,
                GithubOAuth2UserService githubOAuth2UserService,
                GithubOAuthSuccessHandler githubOAuthSuccessHandler,
                GithubOAuthFailureHandler githubOAuthFailureHandler)
                throws Exception {

        http
                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .securityContext(securityContext ->
                        securityContext.securityContextRepository(
                                new NullSecurityContextRepository()))
                .requestCache(requestCache ->
                        requestCache.requestCache(new NullRequestCache()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth

                        // Public authentication and registration
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/github"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/oauth/exchange"
                        ).permitAll()

                        .requestMatchers(
                                "/oauth2/**",
                                "/login/oauth2/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/users"
                        ).permitAll()

                        // Block user-list and user-detail endpoints
                        .requestMatchers(
                                "/api/users",
                                "/api/users/**"
                        ).denyAll()

                        // Job seeker profile belongs only to the authenticated job seeker
                        .requestMatchers(
                                "/api/job-seeker/profile",
                                "/api/job-seeker/profile/**"
                        ).hasRole("JOB_SEEKER")

                        // Public job listings and job details
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/jobs",
                                "/api/jobs/"
                        ).permitAll()

                        // Recruiter-only: view own jobs
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/jobs/my"
                        ).hasRole("RECRUITER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/jobs/*"
                        ).permitAll()

                        // Recruiter-only job management
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/jobs"
                        ).hasRole("RECRUITER")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/jobs/*"
                        ).hasRole("RECRUITER")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/jobs/*"
                        ).hasRole("RECRUITER")

                        // Job seeker applications
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/applications"
                        ).hasRole("JOB_SEEKER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/applications/my"
                        ).hasRole("JOB_SEEKER")

                        // Recruiter application management
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/applications/*/status"
                        ).hasRole("RECRUITER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/applications"
                        ).hasRole("RECRUITER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/applications/*/profile"
                        ).hasRole("RECRUITER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/applications/*"
                        ).hasRole("RECRUITER")

                        // Allow Spring Boot to return the original error response
                        .requestMatchers("/error").permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(
                                (request, response, authException) ->
                                        response.sendError(
                                                HttpServletResponse.SC_UNAUTHORIZED,
                                                "Authentication required or token is invalid"
                                        )
                        )
                        .accessDeniedHandler(
                                (request, response, accessDeniedException) ->
                                        response.sendError(
                                                HttpServletResponse.SC_FORBIDDEN,
                                                "You do not have permission to access this resource"
                                        )
                        )
                )
                .authenticationProvider(authenticationProvider)
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        if (clientRegistrations.getIfAvailable() != null) {
            http.oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(userInfo ->
                            userInfo.userService(githubOAuth2UserService))
                    .successHandler(githubOAuthSuccessHandler)
                    .failureHandler(githubOAuthFailureHandler));
        }

        return http.build();
        }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @org.springframework.beans.factory.annotation.Value(
                    "${app.frontend-url:${FRONTEND_URL:http://localhost:5500}}")
            String frontendUrl) {

        CorsConfiguration configuration = new CorsConfiguration();

        List<String> allowedOrigins = new ArrayList<>(List.of(
                "http://127.0.0.1:5500",
                "http://localhost:5500",
                "http://127.0.0.1:5501",
                "http://localhost:5501"
        ));
        if (!allowedOrigins.contains(frontendUrl)) {
            allowedOrigins.add(frontendUrl);
        }
        configuration.setAllowedOrigins(allowedOrigins);

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}