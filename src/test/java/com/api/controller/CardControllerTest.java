package com.api.controller;

import com.api.config.PermissionChecker;
import com.api.config.enums.CardStatus;
import com.api.dto.*;
import com.api.dto.jwt.JwtRequestDto;
import com.api.dto.jwt.JwtResponseDto;
import com.api.repository.CardRepository;
import com.api.repository.UserRepository;
import com.api.service.AuthServiceImpl;
import com.api.service.interfaces.CardService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CardControllerTest {

    record InvalidDtoCase(String name, Object dto) {
        @Override
        public String toString() {
            return name;
        }}

    @LocalServerPort
    private int port;
    @Autowired
    private UserRepository userRepository; // for real users from DB
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private AuthServiceImpl authService;
    @Autowired
    private TestRestTemplate restTemplate;
    @MockBean
    private CardRepository cardRepository;
    @MockBean
    private CardService cardService;

    private WebTestClient webTestClient; // for PATCH requests
    private UUID ownerId;
    private UUID nonOwnerId;
    private UUID adminId;
    private UUID cardId;
    private UserDto ownerDto;
    private UserDto nonOwner;
    private UserDto adminDto;
    private CardDto cardDto;

    String baseUrl() {
        return "http://localhost:" + port;
    }

    HttpHeaders getHeadersWithBearerAuth(String token){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    HttpEntity<Object> getHttpEntity(Object body, String token){
        return new HttpEntity<>(body, getHeadersWithBearerAuth(token));
    }

    ResponseEntity<JwtResponseDto> login(String email, String password){
        return restTemplate.postForEntity(
                baseUrl() + "/auth/login",
                JwtRequestDto.builder().email(email).password(password).build(),
                JwtResponseDto.class);
    }

    @BeforeEach
    void setUp() {
        this.webTestClient = WebTestClient.bindToServer() // for PATCH requests
                .baseUrl(baseUrl())
                .build();

        ownerId = UUID.fromString("b2c1d7f9-2a28-4d0b-8a47-1b9f95dee2b2");
        adminId = UUID.fromString("a1d0c6e8-1f17-4f8c-9f36-0a8f84cdd1a1");
        nonOwnerId = UUID.fromString("d4f3a9b1-4c4a-4f2d-6d69-3d1fb7ffe4d4");
        cardId = UUID.randomUUID();
        ownerDto = modelMapper.map(userRepository.findById(ownerId), UserDto.class);
        adminDto = modelMapper.map(userRepository.findById(adminId), UserDto.class);
        nonOwner = modelMapper.map(userRepository.findById(nonOwnerId), UserDto.class);
        cardDto = CardDto.builder()
                .id(cardId)
                .number("1111-1111-1111-1111")
                .owner(ownerDto)
                .status(CardStatus.active)
                .expirationDate(LocalDateTime.of(2027,12,30,0,0,0))
                .balance(BigDecimal.valueOf(7530.00))
                .transactionLimitPerDay(BigDecimal.valueOf(1500.00))
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        authService.getRefreshTokensStorage().clear();
    }

    @Nested
    class getCardById {
        @Test
        void admin_success(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(adminDto.getEmail(),adminDto.getPassword());
            when(cardService.getCardById(any(UUID.class))).thenReturn(cardDto);

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards",
                    HttpMethod.POST,
                    getHttpEntity(new IdDto(cardId),
                            jwtResponseDto.getBody().getAccessToken()),
                    CardDto.class
            );

            assertEquals(HttpStatus.OK, cardResponseEntity.getStatusCode());
            assertNotNull(cardResponseEntity.getBody());
            assertEquals(cardId, cardResponseEntity.getBody().getId());

        }
        @Test
        void owner_success(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(ownerDto.getEmail(), ownerDto.getPassword());
            when(cardRepository.existsByIdAndOwnerEmail(cardId, ownerDto.getEmail())).thenReturn(true);
            when(cardService.getCardById(any(UUID.class))).thenReturn(cardDto);

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards",
                    HttpMethod.POST,
                    getHttpEntity(
                            new IdDto(cardId),
                            jwtResponseDto.getBody().getAccessToken()),
                    CardDto.class
            );

            assertEquals(HttpStatus.OK, cardResponseEntity.getStatusCode());
            assertNotNull(cardResponseEntity.getBody());
            assertEquals(cardId, cardResponseEntity.getBody().getId());

        }
        @Test
        void nonOwner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(nonOwner.getEmail(), nonOwner.getPassword());

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards",
                    HttpMethod.POST,
                    getHttpEntity(
                            new IdDto(cardId),
                            jwtResponseDto.getBody().getAccessToken()),
                    CardDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
        @Test
        void unauthenticatedUser_shouldThrow403(){
            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards",
                    HttpMethod.POST,
                    getHttpEntity(
                            new IdDto(cardId),
                            null),
                    CardDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
    }

    @Nested
    class addCard{
        private static Stream<Arguments> invalidPostCardDtos() {
            return Stream.of(
                    Arguments.of(new InvalidDtoCase(
                            "number - out of pattern | owner - null",
                            CardDtoNoId.builder()
                                    .number("dkkf-4566")
                                    .owner(null)
                                    .balance(BigDecimal.valueOf(55.25))
                                    .status(CardStatus.expired)
                                    .transactionLimitPerDay(BigDecimal.valueOf(800.00))
                                    .expirationDate(LocalDateTime.of(2024, 12,12,00,00))
                                    .build()
                    )),
                    Arguments.of(new InvalidDtoCase(
                            "number - out of pattern | owner - null",
                            CardDtoNoId.builder()
                                    .number("1234-4566-4566-4566")
                                    .owner(null)
                                    .balance(BigDecimal.valueOf(55.25))
                                    .status(CardStatus.expired)
                                    .transactionLimitPerDay(BigDecimal.valueOf(800.00))
                                    .expirationDate(LocalDateTime.of(2024, 12,12,00,00))
                                    .build()
                    )),
                    Arguments.of(new InvalidDtoCase(
                            "owner - null | balance - minus | transactionLimitPerDay - under 100",
                            CardDtoNoId.builder()
                                    .number("1234-4566-4566-4566")
                                    .owner(null)
                                    .balance(BigDecimal.valueOf(-55.25))
                                    .status(CardStatus.active)
                                    .transactionLimitPerDay(BigDecimal.valueOf(50.00))
                                    .expirationDate(LocalDateTime.of(2024, 12,12,00,00))
                                    .build()
                    )),
                    Arguments.of(new InvalidDtoCase(
                            "owner - null | transactionLimitPerDay - null",
                            CardDtoNoId.builder()
                                    .number("1234-4566-4566-4566")
                                    .owner(null)
                                    .balance(BigDecimal.valueOf(55.25))
                                    .status(CardStatus.active)
                                    .transactionLimitPerDay(null)
                                    .expirationDate(null)
                                    .build()
                    ))
            );
        }
        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidPostCardDtos")
        void invalidDto_shouldThrow400(InvalidDtoCase invalidDtoCase){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(adminDto.getEmail(),adminDto.getPassword());
            when(cardService.addCard(any(CardDtoNoId.class))).thenReturn(cardDto);

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards/new",
                    HttpMethod.POST,
                    getHttpEntity(invalidDtoCase.dto(),
                            jwtResponseDto.getBody().getAccessToken()),
                    CardDto.class
            );

            assertEquals(HttpStatus.BAD_REQUEST, cardResponseEntity.getStatusCode());
        }
        @Test
        void admin_success(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(adminDto.getEmail(),adminDto.getPassword());
            when(cardService.addCard(any(CardDtoNoId.class))).thenReturn(cardDto);

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards/new",
                    HttpMethod.POST,
                    getHttpEntity(modelMapper.map(cardDto,CardDtoNoId.class),
                            jwtResponseDto.getBody().getAccessToken()),
                    CardDto.class
            );

            assertEquals(HttpStatus.CREATED, cardResponseEntity.getStatusCode());
            assertNotNull(cardResponseEntity.getBody());
            assertEquals(cardId, cardResponseEntity.getBody().getId());
        }
        @Test
        void owner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(ownerDto.getEmail(),ownerDto.getPassword());

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards/new",
                    HttpMethod.POST,
                    getHttpEntity(modelMapper.map(cardDto,CardDtoNoId.class),
                            jwtResponseDto.getBody().getAccessToken()),
                    CardDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
        @Test
        void nonOwner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(nonOwner.getEmail(),nonOwner.getPassword());

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards/new",
                    HttpMethod.POST,
                    getHttpEntity(modelMapper.map(cardDto,CardDtoNoId.class),
                            jwtResponseDto.getBody().getAccessToken()),
                    CardDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
        @Test
        void unauthenticatedUser_shouldThrow403(){

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards/new",
                    HttpMethod.POST,
                    getHttpEntity(modelMapper.map(cardDto,CardDtoNoId.class),
                            null),
                    CardDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
    }

    @Nested
    class updateCard {
        private static Stream<Arguments> invalidPutCardDtos() {
            return Stream.of(
                    Arguments.of(new InvalidDtoCase(
                            "number - out of pattern | owner - null",
                            CardDto.builder()
                                    .id(UUID.randomUUID())
                                    .number("dkkf-4566")
                                    .owner(null)
                                    .balance(BigDecimal.valueOf(55.25))
                                    .status(CardStatus.expired)
                                    .transactionLimitPerDay(BigDecimal.valueOf(800.00))
                                    .expirationDate(LocalDateTime.of(2024, 12,12,00,00))
                                    .build()
                    )),
                    Arguments.of(new InvalidDtoCase(
                            "number - out of pattern | owner - null",
                            CardDto.builder()
                                    .id(UUID.randomUUID())
                                    .number("1234-4566-4566-4566")
                                    .owner(null)
                                    .balance(BigDecimal.valueOf(55.25))
                                    .status(CardStatus.expired)
                                    .transactionLimitPerDay(BigDecimal.valueOf(800.00))
                                    .expirationDate(LocalDateTime.of(2024, 12,12,00,00))
                                    .build()
                    )),
                    Arguments.of(new InvalidDtoCase(
                            "owner - null | balance - minus | transactionLimitPerDay - under 100",
                            CardDto.builder()
                                    .id(UUID.randomUUID())
                                    .number("1234-4566-4566-4566")
                                    .owner(null)
                                    .balance(BigDecimal.valueOf(-55.25))
                                    .status(CardStatus.active)
                                    .transactionLimitPerDay(BigDecimal.valueOf(50.00))
                                    .expirationDate(LocalDateTime.of(2024, 12,12,00,00))
                                    .build()
                    )),
                    Arguments.of(new InvalidDtoCase(
                            "owner - null",
                            CardDto.builder()
                                    .id(UUID.randomUUID())
                                    .number("1234-4566-4566-4566")
                                    .owner(null)
                                    .balance(BigDecimal.valueOf(55.25))
                                    .status(CardStatus.active)
                                    .transactionLimitPerDay(null)
                                    .expirationDate(null)
                                    .build()
                    )),
                    Arguments.of(new InvalidDtoCase(
                            "id - null | owner - null",
                            CardDto.builder()
                                    .id(null)
                                    .number("1234-4566-4566-4566")
                                    .owner(null)
                                    .balance(BigDecimal.valueOf(55.25))
                                    .status(CardStatus.active)
                                    .transactionLimitPerDay(BigDecimal.valueOf(850.00))
                                    .expirationDate(null)
                                    .build()
                    ))
            );
        }
        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidPutCardDtos")
        void invalidDto_shouldThrow400(InvalidDtoCase invalidDtoCase){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(adminDto.getEmail(),adminDto.getPassword());
            when(cardService.updateCard(any(CardDto.class))).thenReturn(cardDto);

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards",
                    HttpMethod.PUT,
                    getHttpEntity(invalidDtoCase.dto(),
                            jwtResponseDto.getBody().getAccessToken()),
                    CardDto.class
            );

            assertEquals(HttpStatus.BAD_REQUEST, cardResponseEntity.getStatusCode());
        }
        @Test
        void admin_success(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(adminDto.getEmail(),adminDto.getPassword());
            when(cardService.updateCard(any(CardDto.class))).thenReturn(cardDto);

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards",
                    HttpMethod.PUT,
                    getHttpEntity(modelMapper.map(cardDto,CardDto.class),
                            jwtResponseDto.getBody().getAccessToken()),
                    CardDto.class
            );

            assertEquals(HttpStatus.OK, cardResponseEntity.getStatusCode());
            assertNotNull(cardResponseEntity.getBody());
            assertEquals(cardId, cardResponseEntity.getBody().getId());
        }
        @Test
        void owner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(ownerDto.getEmail(),ownerDto.getPassword());
            when(cardService.updateCard(any(CardDto.class))).thenReturn(cardDto);

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards",
                    HttpMethod.PUT,
                    getHttpEntity(modelMapper.map(cardDto,CardDto.class),
                            jwtResponseDto.getBody().getAccessToken()),
                    CardDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
        @Test
        void nonOwner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(nonOwner.getEmail(),nonOwner.getPassword());
            when(cardService.updateCard(any(CardDto.class))).thenReturn(cardDto);

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards",
                    HttpMethod.PUT,
                    getHttpEntity(modelMapper.map(cardDto,CardDto.class),
                            jwtResponseDto.getBody().getAccessToken()),
                    CardDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
        @Test
        void unauthenticatedUser_shouldThrow403(){
            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards",
                    HttpMethod.PUT,
                    getHttpEntity(modelMapper.map(cardDto,CardDto.class),
                            null),
                    CardDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }

    }

    @Nested
    class updateCardStatus {
        // TODO: @ParameterizedTest with @MethodSource("")
        void invalidDto_shouldThrow400(){

        }
        @Test
        void admin_success(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(adminDto.getEmail(),adminDto.getPassword());
            doNothing().when(cardService).updateCardStatus(any(UUID.class), any(String.class));

            webTestClient.patch()
                    .uri("/cards/status")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getBody().getAccessToken())
                    .bodyValue(new CardIdStatusDto(cardId, CardStatus.blocked))
                    .exchange()
                    .expectStatus().isOk();
        }
        @Test
        void owner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(ownerDto.getEmail(),ownerDto.getPassword());
            doNothing().when(cardService).updateCardStatus(any(UUID.class), any(String.class));

            webTestClient.patch()
                    .uri("/cards/status")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getBody().getAccessToken())
                    .bodyValue(new CardIdStatusDto(cardId, CardStatus.blocked))
                    .exchange()
                    .expectStatus().isForbidden();
        }
        @Test
        void nonOwner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(nonOwner.getEmail(),nonOwner.getPassword());
            doNothing().when(cardService).updateCardStatus(any(UUID.class), any(String.class));

            webTestClient.patch()
                    .uri("/cards/status")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getBody().getAccessToken())
                    .bodyValue(new CardIdStatusDto(cardId, CardStatus.blocked))
                    .exchange()
                    .expectStatus().isForbidden();
        }
        @Test
        void unauthenticatedUser_shouldThrow403(){
            webTestClient.patch()
                    .uri("/cards/status")
                    .bodyValue(new CardIdStatusDto(cardId, CardStatus.blocked))
                    .exchange()
                    .expectStatus().isForbidden();
        }

    }

    @Nested
    class updateCardsTransactionLimitPerDayById {
        // TODO: @ParameterizedTest with @MethodSource("")
        void invalidDto_shouldThrow400(){

        }
        @Test
        void admin_success(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(adminDto.getEmail(),adminDto.getPassword());
            doNothing().when(cardService).updateCardsTransactionLimitPerDayById(any(UUID.class), any(BigDecimal.class));

            webTestClient.patch()
                    .uri("/cards/transactionLimitPerDay")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getBody().getAccessToken())
                    .bodyValue(new CardIdLimitDto(cardId, cardDto.getTransactionLimitPerDay()))
                    .exchange()
                    .expectStatus().isOk();
        }
        @Test
        void owner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(ownerDto.getEmail(),ownerDto.getPassword());
            doNothing().when(cardService).updateCardsTransactionLimitPerDayById(any(UUID.class), any(BigDecimal.class));

            webTestClient.patch()
                    .uri("/cards/transactionLimitPerDay")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getBody().getAccessToken())
                    .bodyValue(new CardIdLimitDto(cardId, cardDto.getTransactionLimitPerDay()))
                    .exchange()
                    .expectStatus().isForbidden();
        }
        @Test
        void nonOwner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(nonOwner.getEmail(),nonOwner.getPassword());
            doNothing().when(cardService).updateCardsTransactionLimitPerDayById(any(UUID.class), any(BigDecimal.class));

            webTestClient.patch()
                    .uri("/cards/transactionLimitPerDay")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getBody().getAccessToken())
                    .bodyValue(new CardIdLimitDto(cardId, cardDto.getTransactionLimitPerDay()))
                    .exchange()
                    .expectStatus().isForbidden();
        }
        @Test
        void unauthenticatedUser_shouldThrow403(){
            webTestClient.patch()
                    .uri("/cards/transactionLimitPerDay")
                    .bodyValue(new CardIdLimitDto(cardId, cardDto.getTransactionLimitPerDay()))
                    .exchange()
                    .expectStatus().isForbidden();
        }

    }

    @Nested
    class deleteCardById{
        @Test
        void admin_success(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(adminDto.getEmail(),adminDto.getPassword());
            doNothing().when(cardService).deleteCardById(any(UUID.class));

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards",
                    HttpMethod.DELETE,
                    getHttpEntity(new IdDto(cardId),
                            jwtResponseDto.getBody().getAccessToken()),
                    CardDto.class
            );

            assertEquals(HttpStatus.OK, cardResponseEntity.getStatusCode());
            assertNull(cardResponseEntity.getBody());
        }
        @Test
        void owner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(ownerDto.getEmail(),ownerDto.getPassword());
            doNothing().when(cardService).deleteCardById(any(UUID.class));

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards",
                    HttpMethod.DELETE,
                    getHttpEntity(new IdDto(cardId),
                            jwtResponseDto.getBody().getAccessToken()),
                    CardDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
        @Test
        void nonOwner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(nonOwner.getEmail(),nonOwner.getPassword());
            doNothing().when(cardService).deleteCardById(any(UUID.class));

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards",
                    HttpMethod.DELETE,
                    getHttpEntity(new IdDto(cardId),
                            jwtResponseDto.getBody().getAccessToken()),
                    CardDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
        @Test
        void unauthenticatedUser_shouldThrow403(){
            doNothing().when(cardService).deleteCardById(any(UUID.class));

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards",
                    HttpMethod.DELETE,
                    getHttpEntity(new IdDto(cardId),
                            null),
                    CardDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }

    }

    @Nested
    class findAll {
        @Test
        void admin_success(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(adminDto.getEmail(),adminDto.getPassword());
            when(cardService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(cardDto)));

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards/all",
                    HttpMethod.GET,
                    getHttpEntity(null,
                            jwtResponseDto.getBody().getAccessToken()),
                    CardDto.class
            );

            assertEquals(HttpStatus.OK, cardResponseEntity.getStatusCode());
        }
        @Test
        void owner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(ownerDto.getEmail(),ownerDto.getPassword());
            when(cardService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(cardDto)));

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards/all",
                    HttpMethod.GET,
                    getHttpEntity(null,
                            jwtResponseDto.getBody().getAccessToken()),
                    CardDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
        @Test
        void nonOwner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(nonOwner.getEmail(),nonOwner.getPassword());
            when(cardService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(cardDto)));

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards/all",
                    HttpMethod.GET,
                    getHttpEntity(null,
                            jwtResponseDto.getBody().getAccessToken()),
                    CardDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
        @Test
        void unauthenticatedUser_shouldThrow403(){
            when(cardService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(cardDto)));

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards/all",
                    HttpMethod.GET,
                    getHttpEntity(null,
                            null),
                    CardDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }

    }

    @Nested
    class findAllByOwnerId {
        @Test
        void admin_success(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(adminDto.getEmail(),adminDto.getPassword());
            when(cardService.findAllByOwnerId(any(UUID.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(cardDto)));

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards/all/owner",
                    HttpMethod.POST,
                    getHttpEntity(new IdDto(ownerId),
                            jwtResponseDto.getBody().getAccessToken()),
                    CardDto.class
            );

            assertEquals(HttpStatus.OK, cardResponseEntity.getStatusCode());
        }
        @Test
        void owner_success(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(ownerDto.getEmail(),ownerDto.getPassword());
            when(cardService.findAllByOwnerId(any(UUID.class), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(cardDto)));

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards/all/owner",
                    HttpMethod.POST,
                    getHttpEntity(new IdDto(ownerId),
                            jwtResponseDto.getBody().getAccessToken()),
                    CardDto.class
            );

            assertEquals(HttpStatus.OK, cardResponseEntity.getStatusCode());
        }
        @Test
        void nonOwner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(nonOwner.getEmail(),nonOwner.getPassword());
            when(cardService.findAllByOwnerId(any(UUID.class), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(cardDto)));

            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards/all/owner",
                    HttpMethod.POST,
                    getHttpEntity(new IdDto(ownerId),
                            jwtResponseDto.getBody().getAccessToken()),
                    CardDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
        @Test
        void unauthenticatedUser_shouldThrow403(){
            ResponseEntity<CardDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/cards/all/owner",
                    HttpMethod.POST,
                    getHttpEntity(new IdDto(ownerId),
                            null),
                    CardDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
    }
}