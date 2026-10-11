package com.atif.jobportal.config;

import com.atif.jobportal.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;


@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
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
            AuthenticationProvider authenticationProvider)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(auth -> auth

                        // Public registration
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()

                        // Public job browsing
                        .requestMatchers(HttpMethod.GET, "/api/jobs/**").permitAll()

                        // Recruiters manage jobs
                        .requestMatchers(
                                HttpMethod.POST, "/api/jobs"
                        ).hasRole("RECRUITER")
                        .requestMatchers(
                                HttpMethod.PUT, "/api/jobs/**"
                        ).hasRole("RECRUITER")
                        .requestMatchers(
                                HttpMethod.DELETE, "/api/jobs/**"
                        ).hasRole("RECRUITER")

                        // Job seekers submit applications
                        .requestMatchers(
                                HttpMethod.POST, "/api/applications"
                        ).hasRole("JOB_SEEKER")

                        // Recruiters review and update applications
                        .requestMatchers(
                                HttpMethod.PUT, "/api/applications/*/status"
                        ).hasRole("RECRUITER")

                        // All remaining endpoints require login
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .httpBasic(basic -> {});

        return http.build();
    }
}
