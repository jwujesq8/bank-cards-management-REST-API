package com.api.service.auth;

import com.api.dto.jwt.JwtRequestDto;
import com.api.dto.jwt.JwtResponseDto;
import com.api.entity.User;
import com.api.exception.AuthException;
import com.api.exception.ForbiddenException;
import com.api.exception.OkException;
import com.api.security.RefreshTokenStore;
import com.api.service.interfaces.UserService;
import com.api.service.validation.UserValidator;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Class AuthServiceImpl
 *
 * Service implementation for handling authentication operations including login, logout, and token management.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl {

    private final TokenService tokenService;
    private final RefreshTokenStore tokenStore;
    private final UserValidator userValidator;
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    public JwtResponseDto login(@Valid @NotNull JwtRequestDto dto) {
        final User user = userValidator.getUserByEmailOtThrowBadRequest(dto.getEmail());

        if (tokenStore.contains(user.getEmail())) {
            throw new OkException("User is already logged in");
        }

        if (!userValidator.doesUserPasswordEqualTo(user, dto.getPassword())) {
            throw new AuthException("Wrong password");
        }

        final String accessToken = tokenService.generateAccessToken(user);
        final String refreshToken = tokenService.generateRefreshToken(user);
        tokenStore.putIfAbsent(user.getEmail(), refreshToken);

        log.info("{} is logged in", user.getEmail());
        return new JwtResponseDto(accessToken, refreshToken);
    }

    public JwtResponseDto getNewAccessToken(@NotNull String refreshToken) {
        if (!tokenService.validateRefreshToken(refreshToken)) {
            throw new AuthException("Non valid refresh token");
        }

        final Claims claims = tokenService.getRefreshClaims(refreshToken);
        final String email = claims.getSubject();

        if (!tokenStore.validateToken(email, refreshToken)) {
            throw new AuthException("Wrong refresh token");
        }

        final User user = userValidator.getUserByEmailOtThrowForbidden(email);

        final String newAccessToken = tokenService.generateAccessToken(user);
        tokenStore.clearRefreshToken(user.getEmail());

        log.info("{} got new access token", user.getEmail());
        return new JwtResponseDto(newAccessToken, null);
    }

    public JwtResponseDto refresh(@NotNull String refreshToken) {
        if (!tokenService.validateRefreshToken(refreshToken)) {
            throw new AuthException("Non valid refresh token");
        }

        final Claims claims = tokenService.getRefreshClaims(refreshToken);
        final String email = claims.getSubject();

        if (!tokenStore.validateToken(email, refreshToken)) {
            throw new AuthException("Wrong refresh token");
        }

        final User user = userValidator.getUserByEmailOtThrowForbidden(email);

        final String newAccessToken = tokenService.generateAccessToken(user);
        final String newRefreshToken = tokenService.generateRefreshToken(user);
        tokenStore.updateToken(user.getEmail(), refreshToken, newRefreshToken);

        log.info("{} got new access and refresh token", user.getEmail());
        return new JwtResponseDto(newAccessToken, newRefreshToken);
    }

    public void logout(@NotNull String refreshToken) {
        if (!tokenService.validateRefreshToken(refreshToken)) {
            throw new AuthException("Non valid refresh token");
        }

        final Claims claims = tokenService.getRefreshClaims(refreshToken);
        final String email = claims.getSubject();

        if (!tokenStore.validateToken(email, refreshToken)) {
            throw new OkException("User is already logged out");
        }

        final User user = userValidator.getUserByEmailOtThrowForbidden(email);

        tokenStore.invalidate(user.getEmail());
        log.info("{} is logged out", user.getEmail());
    }

    public boolean isUserLoggedIn(String email) {
        return tokenStore.contains(email);
    }
}