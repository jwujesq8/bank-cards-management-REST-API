package com.api.controller;

import com.api.config.enums.Role;
import com.api.dto.UserDto;
import com.api.dto.jwt.JwtRequestDto;
import com.api.dto.jwt.JwtResponseDto;
import com.api.dto.jwt.RefreshJwtRequestDto;
import com.api.entity.User;
import com.api.repository.UserRepository;
import com.api.service.AuthServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.ResourceAccessException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @LocalServerPort
    private int port;
    @MockBean
    private UserRepository userRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private AuthServiceImpl authService;
    private User userDB;
    private UUID userId;
//    private UserDto userDtoDB;

    String baseUrl() {
        return "http://localhost:" + port;
    }

    HttpEntity<RefreshJwtRequestDto> getRequestEntity(JwtResponseDto jwtResponseDto){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtResponseDto.getAccessToken());
        return new HttpEntity<>(RefreshJwtRequestDto.builder()
                .refreshJwtRequest(jwtResponseDto.getRefreshToken())
                .build(), headers);
    }

    ResponseEntity<JwtResponseDto> login(JwtRequestDto jwtRequestDto){
        return restTemplate.postForEntity(
                baseUrl() + "/auth/login",
                jwtRequestDto,
                JwtResponseDto.class);
    }

    @BeforeEach
    void setUp(){
        userId = UUID.randomUUID();
        userDB = User.builder()
                .id(userId)
                .fullName("User Test")
                .email("user@gmail.com")
                .password("123_password")
                .role(Role.ADMIN)
                .build();
        when(userRepository.findByEmail(userDB.getEmail())).thenReturn(Optional.of(userDB));
    }

    @AfterEach
    void tearDown() {
        reset(userRepository);
        SecurityContextHolder.clearContext();
        authService.getRefreshTokensStorage().clear();
    }

    @Nested
    class login {

        @Test
        void success() {
            JwtRequestDto jwtRequestDto = JwtRequestDto.builder()
                    .email("user@gmail.com")
                    .password("123_password")
                    .build();
            ResponseEntity<JwtResponseDto> jwtResponseDto = restTemplate.postForEntity(
                    baseUrl() + "/auth/login",
                    jwtRequestDto,
                    JwtResponseDto.class);
            assertEquals(HttpStatus.OK, jwtResponseDto.getStatusCode());
            assertNotNull(jwtResponseDto.getBody());
            assertNotNull(jwtResponseDto.getBody().getAccessToken());
            assertNotNull(jwtResponseDto.getBody().getRefreshToken());
            assertEquals(3, jwtResponseDto.getBody().getAccessToken().split("\\.").length);
        }

        @Test
        void nonexistedUser_shouldThrowBadRequest() {
            JwtRequestDto jwtRequestDto = JwtRequestDto.builder()
                    .email("nonUser@gmail.com")
                    .password("non_123password")
                    .build();
            ResponseEntity<JwtResponseDto> jwtResponseDto = restTemplate.postForEntity(
                    baseUrl() + "/auth/login",
                    jwtRequestDto,
                    JwtResponseDto.class);
            assertEquals(HttpStatus.BAD_REQUEST, jwtResponseDto.getStatusCode());
            assertNull(jwtResponseDto.getBody().getAccessToken());
        }

        @Test
        void wrongPassword_shouldThrowBadRequest() {
            JwtRequestDto jwtRequestDto = JwtRequestDto.builder()
                    .email("user@gmail.com")
                    .password("wrong_password")
                    .build();
            // TODO: Expected org.springframework.web.client.ResourceAccessException to be thrown, but nothing was thrown.
            assertThrows(ResourceAccessException.class,() -> restTemplate.postForEntity(
                    baseUrl() + "/auth/login",
                    jwtRequestDto,
                    JwtResponseDto.class));
        }
    }


    @Nested
    class getNewAccessToken {

        @Test
        void success() {
            JwtRequestDto jwtRequestDto = JwtRequestDto.builder()
                    .email("user@gmail.com")
                    .password("123_password")
                    .build();
            ResponseEntity<JwtResponseDto> accessAndRefreshToken = restTemplate.postForEntity(
                    baseUrl() + "/auth/login",
                    jwtRequestDto,
                    JwtResponseDto.class);
            RefreshJwtRequestDto refreshJwtRequestDto = RefreshJwtRequestDto.builder()
                    .refreshJwtRequest(accessAndRefreshToken.getBody().getRefreshToken())
                    .build();
            ResponseEntity<JwtResponseDto> newAccessTokenResponse = restTemplate.postForEntity(
                    baseUrl() + "/auth/newAccessToken",
                    refreshJwtRequestDto,
                    JwtResponseDto.class);
            assertEquals(HttpStatus.OK, newAccessTokenResponse.getStatusCode());
            assertNotNull(newAccessTokenResponse.getBody());
            assertNotNull(newAccessTokenResponse.getBody().getAccessToken());
            assertEquals(3, newAccessTokenResponse.getBody().getAccessToken().split("\\.").length);
            assertNull(newAccessTokenResponse.getBody().getRefreshToken());
        }

        @Test
        void unvalidRefreshToken_shouldReturnStatus403() {
            JwtRequestDto jwtRequestDto = JwtRequestDto.builder()
                    .email("user@gmail.com")
                    .password("123_password")
                    .build();
            ResponseEntity<JwtResponseDto> newAccessTokenResponse = restTemplate.postForEntity(
                    baseUrl() + "/auth/newAccessToken",
                    "wrong.refresh.token",
                    JwtResponseDto.class);
            assertEquals(HttpStatus.FORBIDDEN, newAccessTokenResponse.getStatusCode());
            assertNull(newAccessTokenResponse.getBody());
        }


    }

    @Nested
    class refresh {

        @Test
        void success() {
            JwtRequestDto jwtRequestDto = JwtRequestDto.builder()
                    .email("user@gmail.com")
                    .password("123_password")
                    .build();
            ResponseEntity<JwtResponseDto> accessAndRefreshToken = login(jwtRequestDto);
            assertNotNull(accessAndRefreshToken);

            ResponseEntity<JwtResponseDto> refreshResponse = restTemplate.postForEntity(
                    baseUrl() + "/auth/refreshToken",
                    getRequestEntity(accessAndRefreshToken.getBody()), // to set Bearer Auth header
                    JwtResponseDto.class);

            assertEquals(HttpStatus.OK, refreshResponse.getStatusCode());
            assertNotNull(refreshResponse.getBody());
            assertNotNull(refreshResponse.getBody().getAccessToken());
            assertEquals(3, refreshResponse.getBody().getAccessToken().split("\\.").length);
            assertNotNull(refreshResponse.getBody().getRefreshToken());
            assertEquals(3, refreshResponse.getBody().getRefreshToken().split("\\.").length);
        }

        @Test
        void withoutRefreshToken_shouldReturnStatus400() {
            JwtRequestDto jwtRequestDto = JwtRequestDto.builder()
                    .email("user@gmail.com")
                    .password("123_password")
                    .build();
            ResponseEntity<JwtResponseDto> accessAndRefreshToken = login(jwtRequestDto);
            assertNotNull(accessAndRefreshToken);

            assertEquals(HttpStatus.BAD_REQUEST,
                    restTemplate.postForEntity(
                            baseUrl() + "/auth/refreshToken",
                            getRequestEntity(
                                    JwtResponseDto.builder()
                                            .accessToken(accessAndRefreshToken.getBody().getAccessToken())
                                            .refreshToken(null)
                                            .build()),
                            JwtResponseDto.class).getStatusCode());
        }
    }

    @Nested
    class logout {

        @Test
        void success() {
            JwtRequestDto jwtRequestDto = JwtRequestDto.builder()
                    .email("user@gmail.com")
                    .password("123_password")
                    .build();
            ResponseEntity<JwtResponseDto> accessAndRefreshToken = login(jwtRequestDto);
            assertNotNull(accessAndRefreshToken);

            ResponseEntity<Void> response = restTemplate.exchange(
                    baseUrl() + "/auth/logout",
                    HttpMethod.DELETE,
                    getRequestEntity(accessAndRefreshToken.getBody()),
                    Void.class
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }


    }
}