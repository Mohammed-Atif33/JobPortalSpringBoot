package com.atif.jobportal.config;

import com.atif.jobportal.security.CustomUserDetailsService;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http
                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/users"
                        ).permitAll()

                        // Public job listings and job details
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/jobs",
                                "/api/jobs/"
                        ).permitAll()

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

                        // Recruiter application-status updates
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/applications/*/status"
                        ).hasRole("RECRUITER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/applications")
                        .hasRole("RECRUITER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/applications/*")
                        .hasRole("RECRUITER")

                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(
                                (request,
                                 response,
                                 authException)
                                        ->
                                        response.sendError(
                                                HttpServletResponse.SC_UNAUTHORIZED,
                                                "Authentication required or token is invalid"
                                        )
                        )
                        .accessDeniedHandler(
                                (request,
                                 response,
                                 accessDeniedException)
                                        ->
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

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://127.0.0.1:5500",
                "http://localhost:5500",
                "http://127.0.0.1:5501",
                "http://localhost:5501"
        ));

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}