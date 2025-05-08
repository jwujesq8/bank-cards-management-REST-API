package com.api.security;

import com.api.entity.User;
import com.api.exception.AuthException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Class JwtProvider
 *
 * A utility class responsible for generating, validating, and parsing JSON Web Tokens (JWTs).
 * This class provides methods for working with both access and refresh tokens, handling JWT creation and validation,
 * as well as extracting claims from tokens.
 */
@Slf4j
@Component
public class JwtProvider {

    private final SecretKey accessSecretKey;
    private final SecretKey refreshSecretKey;

    /**
     * Constructor that initializes the JwtProvider with the paths to the access and refresh secret key files.
     * It loads the secret keys from the specified file paths and throws an IOException if the files do not exist.
     *
     * @param accessPath the path to the access token secret key file.
     * @param refreshPath the path to the refresh token secret key file.
     * @throws IOException if the specified files are not found.
     */
    public JwtProvider(
            @Value("${jwt.access.path}") String accessPath,
            @Value("${jwt.refresh.path}") String refreshPath,
            ResourceLoader resourceLoader
    ) throws IOException {

        // ACCESS TOKEN
        Resource accessResource = resourceLoader.getResource(accessPath);
        if (accessResource.exists()) {
            String access = new String(accessResource.getInputStream().readAllBytes());
            accessSecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(access));
        } else {
            throw new IOException("Access file not found: " + accessPath);
        }

        // REFRESH TOKEN
        Resource refreshResource = resourceLoader.getResource(refreshPath);
        if (refreshResource.exists()) {
            String refresh = new String(refreshResource.getInputStream().readAllBytes());
            refreshSecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refresh));
        } else {
            throw new IOException("Refresh file not found: " + refreshPath);
        }
    }

    /**
     * Generates an access token for the given user.
     * The token contains the user's email, full name, role, and the issue and expiration dates.
     *
     * @param user the user for whom the access token is generated.
     */
    public String generateAccessToken(@NonNull User user) {

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Minsk"));
        Date currentDate = calendar.getTime();
        Date expirationDate = new Date(currentDate.getTime() + 600000); // 10 min

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationDate)
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .signWith(accessSecretKey)
                .compact();
    }

    /**
     * Generates a refresh token for the given user.
     * The token contains the user's email, role, and the expiration date set to 24 hours from the current time.
     *
     * @param user the user for whom the refresh token is generated.
     */
    public String generateRefreshToken(@NotNull User user){

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Minsk"));
        Date currentDate = calendar.getTime();
        Date expirationDate = new Date(currentDate.getTime() + 86400000);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setExpiration(expirationDate) // 24 hours
                .claim("role", user.getRole())
                .signWith(refreshSecretKey)
                .compact();
    }

    /**
     * Extracts the claims from a given JWT token using the provided secret key.
     *
     * @param token the JWT token to parse.
     * @param secret the secret key used to validate the token.
     */
    private Claims getClaims(@NonNull String token, @NonNull Key secret) {
        return Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extracts the claims from the given access token.
     *
     * @param token the JWT access token to parse.
     */
    public Claims getAccessClaims(@NonNull String token) {
        return getClaims(token, accessSecretKey);
    }

    /**
     * Extracts the claims from the given refresh token.
     *
     * @param token the JWT refresh token to parse.
     */
    public Claims getRefreshClaims(@NonNull String token) {
        return getClaims(token, refreshSecretKey);
    }

    /**
     * Validates the given token using the provided secret key.
     * If the token is expired, malformed, or has an invalid signature, an exception is thrown.
     *
     * @param token the JWT token to validate.
     * @param secret the secret key used to validate the token.
     */
    private boolean validateToken(@NonNull String token, @NonNull Key secret) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException expEx) {
            log.error("Expired JWT token: {}", expEx.getMessage(), expEx);
            throw new AuthException("Token expired: " + expEx.getMessage());
        } catch (UnsupportedJwtException unsEx) {
            log.error("Unsupported JWT token: {}", unsEx.getMessage(), unsEx);
            throw new AuthException("Unsupported JWT: " + unsEx.getMessage());
        } catch (MalformedJwtException mjEx) {
            log.error("Malformed JWT token: {}", mjEx.getMessage(), mjEx);
            throw new AuthException("Malformed JWT: " + mjEx.getMessage());
        } catch (SignatureException sEx) {
            log.error("Invalid JWT signature: {}", sEx.getMessage(), sEx);
            throw new AuthException("Invalid signature: " + sEx.getMessage());
        } catch (Exception e) {
            log.error("Unknown JWT exception: {}", e.getMessage(), e);
            throw new AuthException("Auth exception: " + e.getMessage());
        }
    }

    /**
     * Validates the given access token.
     *
     * @param accessToken the JWT access token to validate.
     */
    public boolean validateAccessToken(@NonNull String accessToken){
        return validateToken(accessToken, accessSecretKey);
    }

    /**
     * Validates the given refresh token.
     *
     * @param refreshToken the JWT refresh token to validate.
     */
    public boolean validateRefreshToken(@NonNull String refreshToken){
        return validateToken(refreshToken, refreshSecretKey);
    }

}

