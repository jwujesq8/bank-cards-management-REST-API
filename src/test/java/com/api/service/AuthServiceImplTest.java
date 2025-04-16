package com.api.service;

import com.api.config.enums.Role;
import com.api.config.jwt.JwtProvider;
import com.api.dto.jwt.JwtRequestDto;
import com.api.dto.jwt.JwtResponseDto;
import com.api.entity.User;
import com.api.exception.ForbiddenException;
import com.api.exception.BadRequestException;
import com.api.exception.OkException;
import com.api.service.interfaces.UserService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {

    private AuthServiceImpl authService;

    @Mock
    private UserService userService;

    @Mock
    private JwtProvider jwtProvider;

    private User user;
    private JwtRequestDto jwtRequestDto;
    private JwtResponseDto jwtResponseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthServiceImpl(userService, jwtProvider);

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
    class loginTest{

        @Test
        public void successfulLogin() {

            when(userService.getUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(jwtProvider.generateAccessToken(user)).thenReturn("accessToken");
            when(jwtProvider.generateRefreshToken(user)).thenReturn("refreshToken");

            JwtResponseDto result = authService.login(jwtRequestDto);

            assertNotNull(result);
            assertEquals("accessToken", result.getAccessToken());
            assertEquals("refreshToken", result.getRefreshToken());
            verify(userService, times(1)).getUserByEmail(user.getEmail());
            verify(jwtProvider, times(1)).generateAccessToken(user);
            verify(jwtProvider, times(1)).generateRefreshToken(user);
        }

        @Test()
        public void userNotFound() {
            when(userService.getUserByEmail(user.getEmail())).thenReturn(Optional.empty());
            assertThrows(BadRequestException.class, () -> authService.login(jwtRequestDto));
        }

        @Test()
        public void wrongPassword() {
            when(userService.getUserByEmail(user.getEmail())).thenReturn(Optional.of(user));

            assertThrows(BadRequestException.class, () -> authService.login(
                    new JwtRequestDto("user@example.com", "wrongPassword")));
        }
    }

    @Nested
    class getNewAccessTokenTest{

        @Test
        public void success() {
            when(jwtProvider.validateRefreshToken("refreshToken")).thenReturn(true);
            Claims claims = mock(Claims.class);
            when(claims.getSubject()).thenReturn("user@gmail.com");
            when(jwtProvider.getRefreshClaims("refreshToken")).thenReturn(claims);
            when(userService.getUserByEmail("user@gmail.com")).thenReturn(Optional.of(user));
            when(jwtProvider.generateAccessToken(user)).thenReturn("newAccessToken");
            authService.getRefreshTokensStorage().put(user.getEmail(), "refreshToken");

            JwtResponseDto result = authService.getNewAccessToken("refreshToken");

            assertNotNull(result);
            assertEquals("newAccessToken", result.getAccessToken());
            verify(jwtProvider, times(1)).validateRefreshToken("refreshToken");
        }

        @Test()
        public void invalidRefreshToken() {
            when(jwtProvider.validateRefreshToken("invalidToken")).thenReturn(false);
            assertThrows(ForbiddenException.class, () -> authService.getNewAccessToken("invalidToken"));
        }
    }

    @Nested
    class refreshTokenTest{

        @Test
        public void success() {
            when(jwtProvider.validateRefreshToken("refreshToken")).thenReturn(true);
            Claims claims = mock(Claims.class);
            when(claims.getSubject()).thenReturn("user@gmail.com");
            when(jwtProvider.getRefreshClaims("refreshToken")).thenReturn(claims);
            when(userService.getUserByEmail("user@gmail.com")).thenReturn(Optional.of(user));
            when(jwtProvider.generateAccessToken(user)).thenReturn("newAccessToken");
            when(jwtProvider.generateRefreshToken(user)).thenReturn("newRefreshToken");
            authService.getRefreshTokensStorage().put(user.getEmail(), "refreshToken");

            JwtResponseDto result = authService.refresh("refreshToken");

            assertNotNull(result);
            assertEquals("newAccessToken", result.getAccessToken());
            assertEquals("newRefreshToken", result.getRefreshToken());
            verify(jwtProvider, times(1)).validateRefreshToken("refreshToken");
        }

        @Test()
        public void invalidRefreshToken() {
            when(jwtProvider.validateRefreshToken("invalidToken")).thenReturn(false);
            assertThrows(ForbiddenException.class, () -> authService.refresh("invalidToken"));
        }
    }

    @Nested
    class logoutTest{

        @Test
        public void success() {
            when(jwtProvider.validateRefreshToken("refreshToken")).thenReturn(true);
            Claims claims = mock(Claims.class);
            when(claims.getSubject()).thenReturn("user@gmail.com");
            when(jwtProvider.getRefreshClaims("refreshToken")).thenReturn(claims);
            when(userService.getUserByEmail("user@gmail.com")).thenReturn(Optional.of(user));
            authService.getRefreshTokensStorage().put(user.getEmail(), "refreshToken");

            authService.logout("refreshToken");

            verify(jwtProvider, times(1)).validateRefreshToken("refreshToken");
            assertFalse(authService.isUserLoggedIn("user@gmail.com"));
        }

        @Test()
        public void invalidRefreshToken() {
            when(jwtProvider.validateRefreshToken("invalidToken")).thenReturn(false);

            assertThrows(ForbiddenException.class, () -> authService.logout("invalidToken"));
        }

        @Test()
        public void alreadyLoggedOut() {
            when(jwtProvider.validateRefreshToken("refreshToken")).thenReturn(true);
            Claims claims = mock(Claims.class);
            when(claims.getSubject()).thenReturn("user@gmail.com");
            when(jwtProvider.getRefreshClaims("refreshToken")).thenReturn(claims);
            when(userService.getUserByEmail("user@gmail.com")).thenReturn(Optional.of(user));
            authService.getRefreshTokensStorage().put(user.getEmail(), "refreshToken");

            authService.logout("refreshToken"); // first logout
            assertThrows(OkException.class, () -> authService.logout("refreshToken"));
        }
    }

    @Nested
    class isUserLoggedInTest{

        @Test
        public void userLoggedIn() {
            when(jwtProvider.validateRefreshToken("refreshToken")).thenReturn(true);
            when(jwtProvider.getRefreshClaims("refreshToken")).thenReturn(mock(Claims.class));
            when(userService.getUserByEmail("user@gmail.com")).thenReturn(Optional.of(user));

            authService.login(jwtRequestDto);

            boolean result = authService.isUserLoggedIn("user@gmail.com");
            assertTrue(result);
        }

        @Test
        public void userNotLoggedIn() {
            boolean result = authService.isUserLoggedIn("user@gmail.com");
            assertFalse(result);
        }
    }
}