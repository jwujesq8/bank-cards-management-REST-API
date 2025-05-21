package com.api.service;

import com.api.config.enums.Role;
import com.api.exception.AuthException;
import com.api.dto.jwt.JwtRequestDto;
import com.api.dto.jwt.JwtResponseDto;
import com.api.entity.User;
import com.api.exception.BadRequestException;
import com.api.exception.OkException;
import com.api.security.RefreshTokenStore;
import com.api.service.auth.AuthServiceImpl;
import com.api.service.auth.TokenService;
import com.api.service.validation.UserValidator;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@Slf4j
class AuthServiceImplTest {

    private AuthServiceImpl authService;

    @Mock
    private TokenService tokenService;
    @Mock
    private RefreshTokenStore tokenStore;
    @Mock
    private UserValidator userValidator;
    private User user;
    private JwtRequestDto jwtRequestDto;
    private JwtResponseDto jwtResponseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthServiceImpl(tokenService, tokenStore, userValidator);

        user = User.builder()
                .email("user@gmail.com")
                .password("password")
                .fullName("Name Surname")
                .role(Role.ADMIN)
                .build();
        jwtRequestDto = new JwtRequestDto("user@gmail.com", "password");
        jwtResponseDto = new JwtResponseDto("accessToken", "refreshToken");
    }

    @Nested
    class login {

        @Test
        public void success() {

            when(userValidator.getUserByEmailOrThrowBadRequest(user.getEmail())).thenReturn(user);
            when(tokenStore.contains(user.getEmail())).thenReturn(false);
            when(userValidator.isUserPasswordEqualTo(user, user.getPassword())).thenReturn(true);
            when(tokenService.generateAccessToken(user)).thenReturn("accessToken");
            when(tokenService.generateRefreshToken(user)).thenReturn("refreshToken");

            JwtResponseDto result = authService.login(jwtRequestDto);

            assertNotNull(result);
            assertEquals("accessToken", result.getAccessToken());
            assertEquals("refreshToken", result.getRefreshToken());
            verify(userValidator, times(1)).getUserByEmailOrThrowBadRequest(user.getEmail());
            verify(tokenService, times(1)).generateAccessToken(user);
            verify(tokenService, times(1)).generateRefreshToken(user);
        }

        @Test()
        public void userNotFound_shouldThrowException() {
            when(userValidator.getUserByEmailOrThrowBadRequest(user.getEmail()))
                    .thenThrow(new BadRequestException("There is no such user"));
            assertThrows(BadRequestException.class, () -> authService.login(jwtRequestDto));
        }

        @Test()
        public void wrongPassword_shouldThrowException() {
            when(userValidator.getUserByEmailOrThrowBadRequest(user.getEmail())).thenReturn(user);
            when(tokenStore.contains(user.getEmail())).thenReturn(false);
            when(userValidator.isUserPasswordEqualTo(user, "wrong-password111"))
                    .thenReturn(false);

            assertThrows(AuthException.class, () -> authService.login(
                    new JwtRequestDto(user.getEmail(), "wrong-password111")));
        }
    }

    @Nested
    class getNewAccessTokenTest{

        @Test
        public void success() {
            when(tokenService.validateRefreshToken("refreshToken")).thenReturn(true);
            Claims claims = mock(Claims.class);
            when(claims.getSubject()).thenReturn(user.getEmail());
            when(tokenService.getRefreshClaims("refreshToken")).thenReturn(claims);
            when(userValidator.getUserByEmailOrThrowForbidden(user.getEmail())).thenReturn(user);
            when(tokenStore.validateToken(user.getEmail(), "refreshToken")).thenReturn(true);
            when(tokenService.generateAccessToken(user)).thenReturn("newAccessToken");

            JwtResponseDto result = authService.getNewAccessToken("refreshToken");

            assertNotNull(result);
            assertEquals("newAccessToken", result.getAccessToken());
            verify(tokenService, times(1)).validateRefreshToken("refreshToken");
        }

        @Test()
        public void invalidRefreshToken_shouldThrowException() {
            when(tokenService.validateRefreshToken("invalidToken")).thenReturn(false);

            assertThrows(AuthException.class, () -> authService.getNewAccessToken("invalidToken"));
        }
    }

    @Nested
    class refreshTokenTest{

        @Test
        public void success() {
            when(tokenService.validateRefreshToken("refreshToken")).thenReturn(true);
            Claims claims = mock(Claims.class);
            when(claims.getSubject()).thenReturn(user.getEmail());
            when(tokenService.getRefreshClaims("refreshToken")).thenReturn(claims);
            when(userValidator.getUserByEmailOrThrowForbidden(user.getEmail())).thenReturn(user);
            when(tokenStore.validateToken(user.getEmail(), "refreshToken")).thenReturn(true);
            when(tokenService.generateAccessToken(user)).thenReturn("newAccessToken");
            when(tokenService.generateRefreshToken(user)).thenReturn("newRefreshToken");

            JwtResponseDto result = authService.refresh("refreshToken");

            assertNotNull(result);
            assertEquals("newAccessToken", result.getAccessToken());
            assertEquals("newRefreshToken", result.getRefreshToken());
            verify(tokenService, times(1)).validateRefreshToken("refreshToken");
        }

        @Test()
        public void invalidRefreshToken_shouldThrowException() {
            when(tokenService.validateRefreshToken("invalidToken")).thenReturn(false);
            assertThrows(AuthException.class, () -> authService.refresh("invalidToken"));
        }
    }

    @Nested
    class logoutTest{

        @Test
        public void success() {
            when(tokenService.validateRefreshToken("refreshToken")).thenReturn(true);
            Claims claims = mock(Claims.class);
            when(claims.getSubject()).thenReturn(user.getEmail());
            when(tokenService.getRefreshClaims("refreshToken")).thenReturn(claims);
            when(userValidator.getUserByEmailOrThrowForbidden(user.getEmail())).thenReturn(user);

            authService.logout("refreshToken");

            verify(tokenService, times(1)).validateRefreshToken("refreshToken");
            assertFalse(authService.isUserLoggedIn("user@gmail.com"));
        }

        @Test()
        public void invalidRefreshToken_shouldThrowException() {
            when(tokenService.validateRefreshToken("invalidToken")).thenReturn(false);

            assertThrows(AuthException.class, () -> authService.logout("invalidToken"));
        }
    }

    @Nested
    class isUserLoggedInTest{

        @Test
        public void userLoggedIn() {
            when(tokenStore.contains(user.getEmail())).thenReturn(true);

            boolean result = authService.isUserLoggedIn(user.getEmail());

            assertTrue(result);
        }

        @Test
        public void userNotLoggedIn() {
            boolean result = authService.isUserLoggedIn(user.getEmail());
            assertFalse(result);
        }
    }
}