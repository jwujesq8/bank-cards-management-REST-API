package com.api.controller;

import com.api.dto.error.ErrorMessageResponseDto;
import com.api.dto.jwt.JwtRequestDto;
import com.api.dto.jwt.JwtResponseDto;
import com.api.dto.jwt.RefreshJwtRequestDto;
import com.api.service.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Class AuthController
 *
 * Controller for handling user authentication and JWT token operations.
 * Provides endpoints for login, obtaining new access tokens, refreshing tokens, and logging out.
 */
@RestController
@RequestMapping("/auth")
@Validated
@RequiredArgsConstructor
@Tag(name="Auth controller", description="Control the user authorization (JWT token)")
public class AuthController {

    private final AuthServiceImpl authServiceImpl;

    /**
     * Endpoint for user login.
     * Authenticates the user and returns a JWT access token.
     *
     * @param JwtRequestDto The request body containing user login credentials.
     * @throws ConstraintViolationException if the request body validation fails.
     */
    @Operation(summary = "log in")
    @PostMapping("/login")
    @PreAuthorize("!isAuthenticated()")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful request to log in", content = @Content(schema = @Schema(implementation = JwtResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Unsuccessful log in",  content = @Content(mediaType = "none"))}
    )
    public JwtResponseDto login(@Parameter(description = "user login and password inside the request body", required = true)
            @RequestBody @Valid @NotNull JwtRequestDto JwtRequestDto) throws ConstraintViolationException {
        return authServiceImpl.login(JwtRequestDto);
    }

    /**
     * Endpoint to get a new access token using a valid refresh token.
     * After the new access token is obtained, the used refresh token becomes invalid.
     *
     * @param RefreshJwtRequestDto The request body containing the refresh token.
     */
    @Operation(summary = "get new access token (AFTER THAT USED REFRESH TOKEN WILL BE NON VALID)")
    @PostMapping("/newAccessToken")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful request to get new access token", content = @Content(schema = @Schema(implementation = JwtResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Non valid token", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Authentication error", content = @Content(mediaType = "none"))}
    )
    public JwtResponseDto getNewAccessToken(@Parameter(description = "refresh user token inside the request body", required = true)
            @RequestBody @Valid @NotNull RefreshJwtRequestDto RefreshJwtRequestDto){
        return authServiceImpl.getNewAccessToken(RefreshJwtRequestDto.getRefreshJwtRequest());
    }

    /**
     * Endpoint to refresh both the access and refresh tokens.
     *
     * @param RefreshJwtRequestDto The request body containing the refresh token.
     */
    @Operation(summary = "get new access and refresh token")
    @PostMapping("/refreshToken")
    @SecurityRequirement(name = "JWT")@ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful request to get new access token and refresh token", content = @Content(schema = @Schema(implementation = JwtResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Non valid token", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Authentication error", content = @Content(mediaType = "none"))
    }
    )
    public JwtResponseDto refresh(@Parameter(description = "refresh user token inside the request body", required = true)
            @RequestBody @Valid @NotNull RefreshJwtRequestDto RefreshJwtRequestDto){
        return authServiceImpl.refresh(RefreshJwtRequestDto.getRefreshJwtRequest());
    }

    /**
     * Endpoint for user logout.
     * Invalidates the current refresh token and logs the user out.
     *
     * @param RefreshJwtRequestDto The request body containing the refresh token.
     */
    @Operation(summary = "log out")
    @DeleteMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful request to log out"),
            @ApiResponse(responseCode = "401", description = "Non valid token", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Authentication error")
    }
    )
    public void logout(@Parameter(description = "refresh user token inside the request body", required = true)
            @RequestBody @Valid @NotNull RefreshJwtRequestDto RefreshJwtRequestDto){
        authServiceImpl.logout(RefreshJwtRequestDto.getRefreshJwtRequest());
    }

}
