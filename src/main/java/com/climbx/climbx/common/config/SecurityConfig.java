package com.climbx.climbx.common.config;

import com.climbx.climbx.common.middleware.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth ->
                auth
                    .requestMatchers(HttpMethod.GET, "/api/admin/health-check").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/auth/oauth2/*/callback").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/oauth2/refresh").permitAll()
                    .requestMatchers("/api/auth/me").authenticated()
                    .requestMatchers("/api/auth/signout").authenticated()
                    .requestMatchers("/api/auth/unregister").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/users/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/videos/upload").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/submissions/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/submissions/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/problems/*").authenticated()
                    .requestMatchers(HttpMethod.POST).authenticated()
                    .requestMatchers(HttpMethod.PATCH).authenticated()
                    .requestMatchers(HttpMethod.PUT).authenticated()
                    .requestMatchers(HttpMethod.DELETE).hasRole("ADMIN")
                    .anyRequest().permitAll()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(
                    (req, res, ex2) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                )
                .accessDeniedHandler(new AccessDeniedHandlerImpl())
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
