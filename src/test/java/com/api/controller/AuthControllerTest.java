package com.api.controller;

import com.api.config.enums.Role;
import com.api.dto.jwt.JwtRequestDto;
import com.api.dto.jwt.JwtResponseDto;
import com.api.dto.jwt.RefreshJwtRequestDto;
import com.api.entity.User;
import com.api.repository.UserRepository;
import com.api.service.auth.AuthServiceImpl;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
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

    ResponseEntity<JwtResponseDto> login(String email, String password){
        return restTemplate.postForEntity(
                baseUrl() + "/auth/login",
                JwtRequestDto.builder().email(email).password(password).build(),
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
            ResponseEntity<JwtResponseDto> jwtResponseDtoResponseEntity = login("user@gmail.com", "123_password");
            RefreshJwtRequestDto refreshJwtRequestDto = RefreshJwtRequestDto.builder()
                    .refreshJwtRequest(jwtResponseDtoResponseEntity.getBody().getRefreshToken())
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
        void invalidRefreshToken_shouldThrowException() {
            ResponseEntity<JwtResponseDto> jwtResponseDtoResponseEntity = login("user@gmail.com", "123_password");
            jwtResponseDtoResponseEntity.getBody().setRefreshToken(
                    "12"+jwtResponseDtoResponseEntity.getBody().getRefreshToken().substring(2));

            assertThrows(ResourceAccessException.class, () -> restTemplate.postForEntity(
                    baseUrl() + "/auth/newAccessToken",
                    getRequestEntity(jwtResponseDtoResponseEntity.getBody()), // to set Bearer Auth header
                    JwtResponseDto.class));

        }
    }

    @Nested
    class refresh {

        @Test
        void success() {
            ResponseEntity<JwtResponseDto> responseDtoResponseEntity = login("user@gmail.com","123_password");
            assertNotNull(responseDtoResponseEntity);

            ResponseEntity<JwtResponseDto> refreshResponse = restTemplate.postForEntity(
                    baseUrl() + "/auth/refreshToken",
                    getRequestEntity(responseDtoResponseEntity.getBody()), // to set Bearer Auth header
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
            ResponseEntity<JwtResponseDto> responseDtoResponseEntity = login("user@gmail.com","123_password");
            assertNotNull(responseDtoResponseEntity);

            assertEquals(HttpStatus.BAD_REQUEST,
                    restTemplate.postForEntity(
                            baseUrl() + "/auth/refreshToken",
                            getRequestEntity( // to set Bearer Auth header
                                    JwtResponseDto.builder()
                                            .accessToken(responseDtoResponseEntity.getBody().getAccessToken())
                                            .refreshToken(null)
                                            .build()),
                            JwtResponseDto.class).getStatusCode());
        }
    }

    @Nested
    class logout {

        @Test
        void success() {
            ResponseEntity<JwtResponseDto> responseDtoResponseEntity = login("user@gmail.com","123_password");
            assertNotNull(responseDtoResponseEntity);

            ResponseEntity<Void> response = restTemplate.exchange(
                    baseUrl() + "/auth/logout",
                    HttpMethod.DELETE,
                    getRequestEntity(responseDtoResponseEntity.getBody()), // to set Bearer Auth header
                    Void.class
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }
}