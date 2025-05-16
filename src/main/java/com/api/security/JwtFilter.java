package com.api.security;

import com.api.config.enums.Role;
import com.api.service.auth.AuthServiceImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Class JwtFilter
 *
 * A filter that processes incoming requests with a JWT (JSON Web Token).
 * It checks for the presence of a token in the "Authorization" header and validates it.
 * If the token is valid, it extracts the user's details from the token and sets
 * the authentication in the {@link SecurityContextHolder}.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private final JwtProvider jwtProvider;
    private final AuthServiceImpl authServiceImpl;

    /**
     * Filters incoming HTTP requests to validate the presence and authenticity of a JWT (JSON Web Token).
     * The method checks the "Authorization" header for a Bearer token, validates the token,
     * and if valid, extracts the user information from the token and sets the authentication
     * in the {@link SecurityContextHolder}.
     *
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @param filterChain The filter chain to pass the request and response to the next filter.
     * @throws ServletException If an error occurs during the request processing.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal (
            jakarta.servlet.http.HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = null;
        final String bearer = request.getHeader(AUTHORIZATION);
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            token = bearer.substring(7);
        }
        if (token != null && jwtProvider.validateAccessToken(token)) {
            final Claims claims = jwtProvider.getAccessClaims(token);

            final JwtAuthentication jwtAuthentication = new JwtAuthentication();
            jwtAuthentication.setEmail(claims.getSubject());
            jwtAuthentication.setRole(Role.valueOf(claims.get("role", String.class)));
            jwtAuthentication.setAuthenticated(authServiceImpl.isUserLoggedIn(claims.getSubject()));
            SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);

        }
        filterChain.doFilter(request, response);
    }

}
