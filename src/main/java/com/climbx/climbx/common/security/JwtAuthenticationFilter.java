package com.climbx.climbx.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtContext jwtContext;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String token = jwtContext.extractTokenFromRequest(request);

            Long userId = jwtContext.extractSubject(token);
            String role = jwtContext.extractRole(token);

            setAuthentication(new AuthenticationInfo(userId, role));
        } catch (Exception e) {
            log.warn("인증되지 않은 요청");
            SecurityContextHolder.clearContext();
        } finally {
            filterChain.doFilter(request, response);
        }
    }

    private void setAuthentication(AuthenticationInfo authInfo) {
        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority(authInfo.role)
        );

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                authInfo.userId,
                null,
                authorities
            );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("사용자 인증 완료: userId={}, role={}", authInfo.userId, authInfo.role);
    }

    private static class AuthenticationInfo {

        final Long userId;
        final String role;

        AuthenticationInfo(Long userId, String role) {
            this.userId = userId;
            this.role = role;
        }
    }
} 