package com.api.service;

import com.api.config.jwt.JwtProvider;
import com.api.dto.jwt.JwtRequestDto;
import com.api.dto.jwt.JwtResponseDto;
import com.api.entity.User;
import com.api.exception.AuthException;
import com.api.exception.BadRequestException;
import com.api.exception.OkException;
import com.api.service.interfaces.UserService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

/**
 * Class AuthServiceImpl
 *
 * Service implementation for handling authentication operations including login, logout, and token management.
 */
@Service
@RequiredArgsConstructor
@Validated
public class AuthServiceImpl {

    private final UserService userService;
    @Getter
    private final Map<String, String> refreshTokensStorage = new HashMap<>();
    private final JwtProvider jwtProvider;
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    /**
     * Handles user login by verifying the email and password, generating an access token, and storing a refresh token.
     *
     * @param jwtRequestDto Contains email and password for authentication.
     * @return A {@link JwtResponseDto} containing the generated access and refresh tokens.
     * @throws BadRequestException If the user is not found.
     * @throws AuthException If the password is incorrect.
     * @throws OkException If the user is already logged in.
     */
    public JwtResponseDto login(@Valid @NotNull JwtRequestDto jwtRequestDto) {
        final User user = userService.getUserByEmail(jwtRequestDto.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if(!refreshTokensStorage.containsKey(user.getEmail())){
            if(user.getPassword().equals(jwtRequestDto.getPassword())){
                final String accessToken = jwtProvider.generateAccessToken(user);
                final String refreshToken = jwtProvider.generateRefreshToken(user);

                refreshTokensStorage.put(user.getEmail(), refreshToken);
                log.info("{} is logged in", user.getEmail());
                return new JwtResponseDto(accessToken, refreshToken);
            }
            else {
                throw new AuthException("Wrong password");
            }
        } else {
            throw new OkException("User is already logged in");
        }
    }

    /**
     * Generates a new access token for a user based on a valid refresh token.
     *
     * @param refreshToken The refresh token to validate and use for generating a new access token.
     * @return A {@link JwtResponseDto} containing the new access token.
     * @throws AuthException If the refresh token is invalid or expired.
     */
    public JwtResponseDto getNewAccessToken(@NotNull String refreshToken){
        if(jwtProvider.validateRefreshToken(refreshToken)){
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String login = claims.getSubject();
            final String refreshTokenDB = refreshTokensStorage.get(login);

            if(refreshTokenDB != null && refreshTokenDB.equals(refreshToken)){
                final User user = userService.getUserByEmail(login)
                        .orElseThrow(() -> new AuthException("User not found"));

                String newAccessToken = jwtProvider.generateAccessToken(user);
                refreshTokensStorage.put(user.getEmail(), null);
                log.info("{} got new access token", user.getEmail());
                return new JwtResponseDto(newAccessToken, null);
            }
            throw new AuthException("Wrong refresh token");
        }
        throw new AuthException("Non valid refresh token");
    }

    /**
     * Refreshes both the access token and refresh token for the user.
     *
     * @param refreshToken The refresh token to validate and use for refreshing both tokens.
     * @return A {@link JwtResponseDto} containing the new access and refresh tokens.
     * @throws AuthException If the refresh token is invalid or expired.
     */
    public JwtResponseDto refresh(@NotNull String refreshToken){
        if(jwtProvider.validateRefreshToken(refreshToken)){
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String login = claims.getSubject();
            final String refreshTokenDB = refreshTokensStorage.get(login);

            if(refreshTokenDB != null && refreshTokenDB.equals(refreshToken)){
                final User user = userService.getUserByEmail(login)
                        .orElseThrow(() -> new AuthException("User not found"));

                String newAccessToken = jwtProvider.generateAccessToken(user);
                String newRefreshToken = jwtProvider.generateRefreshToken(user);
                refreshTokensStorage.put(user.getEmail(), newRefreshToken);
                log.info("{} got new access token and refresh token", user.getEmail());
                return new JwtResponseDto(newAccessToken, newRefreshToken);
            }
            throw new AuthException("Wrong refresh token");
        }
        throw new AuthException("Non valid refresh token");
    }

    /**
     * Logs out the user by invalidating their refresh token.
     *
     * @param refreshToken The refresh token to invalidate.
     * @throws AuthException If the refresh token is invalid or expired.
     * @throws OkException If the user is already logged out.
     */
    public void logout(@NotNull String refreshToken) {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String login = claims.getSubject();
            final String refreshTokenDB = refreshTokensStorage.get(login);

            if (refreshTokenDB != null && refreshTokenDB.equals(refreshToken)) {
                final User user = userService.getUserByEmail(login)
                        .orElseThrow(() -> new AuthException("User not found"));

                refreshTokensStorage.remove(user.getEmail());
                log.info("{} is logged out", user.getEmail());
                return;
            }
            throw new OkException("User is already logged out");
        }
        throw new AuthException("Non valid refresh token");
    }

    /**
     * Checks if a user is logged in by verifying if their refresh token is stored.
     *
     * @param login The email (login) of the user to check.
     * @return {@code true} if the user is logged in, {@code false} otherwise.
     */
    public boolean isUserLoggedIn(String login){
        return refreshTokensStorage.containsKey(login);
    }
}
