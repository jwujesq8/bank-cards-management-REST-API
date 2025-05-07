package com.api.dto.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Class JwtResponseDto
 *
 * Data Transfer Object (DTO) representing a JWT response.
 * This DTO contains the access and refresh tokens used for user authentication.
 */
@Getter
@AllArgsConstructor
@Builder
public class JwtResponseDto {

    /**
     * The type of authentication, which is typically "Bearer" for JWT-based systems.
     */
    @Schema(description = "authentication type", example = "Bearer", minLength = 6, maxLength = 6)
    private final String type = "Bearer";

    /**
     * The access token that is received after a successful login, refresh, or request for a new access token.
     * This token is used for authenticating user requests.
     */
    @NotEmpty(message = "Access token must not be empty or null")
    @Schema(description = "access user token (received after successful login or refresh or get a new access token)",
            example = "your.access.token")
    private String accessToken;

    /**
     * The refresh token that is received after a successful login or refresh.
     * This token is used to obtain a new access token when the current one expires.
     */
    @NotEmpty(message = "Refresh token must not be empty or null")
    @Schema(description = "refresh user token (received after successful login or refresh)",
            example = "your.refresh.token")
    private String refreshToken;

}
