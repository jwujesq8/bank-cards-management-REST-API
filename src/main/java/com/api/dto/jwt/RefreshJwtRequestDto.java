package com.api.dto.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Class RefreshJwtRequestDto
 *
 * Data Transfer Object (DTO) for refreshing JWT tokens.
 * This DTO contains the refresh token that is used to obtain new login and refresh tokens.

 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshJwtRequestDto {

    /**
     * The refresh token that is used to obtain a new login and refresh tokens.
     * This token is necessary when the current access token expires.
     */
    @NotEmpty(message = "Refresh token must not be empty or null")
    @Schema(description = "refresh JWT token (used for getting a new login and refresh tokens)", example = "your.refresh.token")
    private String refreshJwtRequest;
}
