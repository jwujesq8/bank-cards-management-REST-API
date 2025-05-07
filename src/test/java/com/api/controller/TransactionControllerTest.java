package com.api.controller;

import com.api.config.enums.CardStatus;
import com.api.dto.*;
import com.api.dto.jwt.JwtRequestDto;
import com.api.dto.jwt.JwtResponseDto;
import com.api.repository.CardRepository;
import com.api.repository.TransactionRepository;
import com.api.repository.UserRepository;
import com.api.service.AuthServiceImpl;
import com.api.service.interfaces.TransactionService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionControllerTest {

    record InvalidDtoCase(String name, Object dto) {
        @Override
        public String toString() {
            return name;
        }}

    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private AuthServiceImpl authService;
    @Autowired
    private UserRepository userRepository; // for real users from the DB
    @Autowired
    private CardRepository cardRepository; // for real cards from DB
    @MockBean
    private TransactionService transactionService;
    @MockBean
    private TransactionRepository transactionRepository; // for permission checking

    private UUID transactionId;
    private TransactionDto transactionDto;
    private UUID adminId;
    private UserDto adminDto;
    private UUID sourceOwnerId;
    private UserDto sourceOwnerDto;
    private UUID nonOwnerId;
    private UserDto nonOwnerDto;
    private UUID sourceCardId;
    private CardDto sourceCardDto;
    private UUID destinationCardId;
    private CardDto destinationCardDto;
    private PaymentDto paymentDto;

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
        adminId = UUID.fromString("a1d0c6e8-1f17-4f8c-9f36-0a8f84cdd1a1");
        adminDto = modelMapper.map(userRepository.findById(adminId), UserDto.class);

        sourceOwnerId = UUID.fromString("b2c1d7f9-2a28-4d0b-8a47-1b9f95dee2b2");
        sourceOwnerDto = modelMapper.map(userRepository.findById(sourceOwnerId), UserDto.class);

        nonOwnerId = UUID.fromString("d4f3a9b1-4c4a-4f2d-6d69-3d1fb7ffe4d4");
        nonOwnerDto = modelMapper.map(userRepository.findById(nonOwnerId), UserDto.class);

        sourceCardId = UUID.fromString("202bbb02-bbbb-bbbb-bbbb-bbbbbbbbbbb2");
        sourceCardDto = modelMapper.map(cardRepository.findById(sourceCardId), CardDto.class);

        destinationCardId = UUID.fromString("303ccc03-cccc-cccc-cccc-cccccccccc03");
        destinationCardDto = modelMapper.map(cardRepository.findById(sourceCardId), CardDto.class);

        transactionId = UUID.randomUUID();
        transactionDto = TransactionDto.builder()
                .id(transactionId)
                .source(sourceCardDto)
                .destination(destinationCardDto)
                .amount(BigDecimal.valueOf(24.30))
                .localDateTime(LocalDateTime.now())
                .build();

        paymentDto = new PaymentDto(sourceCardId, destinationCardId, BigDecimal.valueOf(23.54));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        authService.getRefreshTokensStorage().clear();
    }

    @Nested
    class getTransactionById {
        @Test
        void admin_success(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(adminDto.getEmail(),adminDto.getPassword());
            when(transactionService.getTransactionById(any(UUID.class))).thenReturn(transactionDto);

            ResponseEntity<TransactionDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions",
                    HttpMethod.POST,
                    getHttpEntity(new IdDto(transactionId),
                            jwtResponseDto.getBody().getAccessToken()),
                    TransactionDto.class
            );

            assertEquals(HttpStatus.OK, cardResponseEntity.getStatusCode());
            assertNotNull(cardResponseEntity.getBody());
            assertEquals(transactionId, cardResponseEntity.getBody().getId());
        }
        @Test
        void sourceOwner_success(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(sourceOwnerDto.getEmail(),sourceOwnerDto.getPassword());
            when(transactionRepository.existsByIdAndSourceOwnerEmail(any(UUID.class), any(String.class))).thenReturn(true);
            when(transactionService.getTransactionById(any(UUID.class))).thenReturn(transactionDto);

            ResponseEntity<TransactionDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions",
                    HttpMethod.POST,
                    getHttpEntity(new IdDto(transactionId),
                            jwtResponseDto.getBody().getAccessToken()),
                    TransactionDto.class
            );

            assertEquals(HttpStatus.OK, cardResponseEntity.getStatusCode());
            assertNotNull(cardResponseEntity.getBody());
            assertEquals(transactionId, cardResponseEntity.getBody().getId());

        }
        @Test
        void nonOwner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(nonOwnerDto.getEmail(),nonOwnerDto.getPassword());
            when(transactionService.getTransactionById(any(UUID.class))).thenReturn(transactionDto);

            ResponseEntity<TransactionDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions",
                    HttpMethod.POST,
                    getHttpEntity(new IdDto(transactionId),
                            jwtResponseDto.getBody().getAccessToken()),
                    TransactionDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
    }

    @Nested
    class addTransaction {
        private static Stream<Arguments> invalidPostTransactionDtos() {
            return Stream.of(
                    Arguments.of(new InvalidDtoCase(
                            "source - null | destination - null",
                            TransactionDtoNoId.builder()
                                    .source(null)
                                    .destination(null)
                                    .amount(BigDecimal.valueOf(50.00))
                                    .localDateTime(LocalDateTime.now())
                                    .build()
                    )),
                    Arguments.of(new InvalidDtoCase(
                            "source - null | destination - null | amount - minus",
                            TransactionDtoNoId.builder()
                                    .source(CardDto.builder().build())
                                    .destination(null)
                                    .amount(BigDecimal.valueOf(-5))
                                    .localDateTime(LocalDateTime.now())
                                    .build()
                    )),
                    Arguments.of(new InvalidDtoCase(
                            "source - null | destination - null | amount - null",
                            TransactionDtoNoId.builder()
                                    .source(null)
                                    .destination(null)
                                    .amount(null)
                                    .localDateTime(LocalDateTime.now())
                                    .build()
                    )),
                    Arguments.of(new InvalidDtoCase(
                            "source - null | destination - null | localDateTime - null",
                            TransactionDtoNoId.builder()
                                    .source(null)
                                    .destination(null)
                                    .amount(BigDecimal.valueOf(50.00))
                                    .localDateTime(null)
                                    .build()
                    ))
            );
        }
        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidPostTransactionDtos")
        void invalidDto_shouldThrow400(InvalidDtoCase invalidDtoCase){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(adminDto.getEmail(),adminDto.getPassword());
            when(transactionService.addTransaction(any(TransactionDtoNoId.class))).thenReturn(transactionDto);

            ResponseEntity<TransactionDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions/new",
                    HttpMethod.POST,
                    getHttpEntity(invalidDtoCase.dto(),
                            jwtResponseDto.getBody().getAccessToken()),
                    TransactionDto.class
            );

            assertEquals(HttpStatus.BAD_REQUEST, cardResponseEntity.getStatusCode());
        }
        @Test
        void admin_success(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(adminDto.getEmail(),adminDto.getPassword());
            when(transactionService.addTransaction(any(TransactionDtoNoId.class))).thenReturn(transactionDto);

            ResponseEntity<TransactionDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions/new",
                    HttpMethod.POST,
                    getHttpEntity(modelMapper.map(transactionDto, TransactionDtoNoId.class),
                            jwtResponseDto.getBody().getAccessToken()),
                    TransactionDto.class
            );

            assertEquals(HttpStatus.CREATED, cardResponseEntity.getStatusCode());
            assertNotNull(cardResponseEntity.getBody());
            assertEquals(transactionId, cardResponseEntity.getBody().getId());
        }
        @Test
        void sourceOwner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(sourceOwnerDto.getEmail(),sourceOwnerDto.getPassword());
            when(transactionService.addTransaction(any(TransactionDtoNoId.class))).thenReturn(transactionDto);

            ResponseEntity<TransactionDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions/new",
                    HttpMethod.POST,
                    getHttpEntity(modelMapper.map(transactionDto, TransactionDtoNoId.class),
                            jwtResponseDto.getBody().getAccessToken()),
                    TransactionDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());

        }
        @Test
        void nonOwner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(nonOwnerDto.getEmail(),nonOwnerDto.getPassword());
            when(transactionService.addTransaction(any(TransactionDtoNoId.class))).thenReturn(transactionDto);

            ResponseEntity<TransactionDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions/new",
                    HttpMethod.POST,
                    getHttpEntity(modelMapper.map(transactionDto, TransactionDtoNoId.class),
                            jwtResponseDto.getBody().getAccessToken()),
                    TransactionDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
    }

    @Nested
    class updateTransaction {
        private static Stream<Arguments> invalidPutTransactionDtos() {
            return Stream.of(
                    Arguments.of(new InvalidDtoCase(
                            "id - null | source - null | destination - null",
                            TransactionDto.builder()
                                    .id(null)
                                    .source(null)
                                    .destination(null)
                                    .amount(BigDecimal.valueOf(50.00))
                                    .localDateTime(LocalDateTime.now())
                                    .build()
                    )),
                    Arguments.of(new InvalidDtoCase(
                            "source - null | destination - null | amount - minus",
                            TransactionDto.builder()
                                    .id(UUID.randomUUID())
                                    .source(CardDto.builder().build())
                                    .destination(null)
                                    .amount(BigDecimal.valueOf(-5))
                                    .localDateTime(LocalDateTime.now())
                                    .build()
                    )),
                    Arguments.of(new InvalidDtoCase(
                            "source - null | destination - null | amount - null",
                            TransactionDto.builder()
                                    .id(UUID.randomUUID())
                                    .source(null)
                                    .destination(null)
                                    .amount(null)
                                    .localDateTime(LocalDateTime.now())
                                    .build()
                    )),
                    Arguments.of(new InvalidDtoCase(
                            "source - null | destination - null | localDateTime - null",
                            TransactionDto.builder()
                                    .id(UUID.randomUUID())
                                    .source(null)
                                    .destination(null)
                                    .amount(BigDecimal.valueOf(50.00))
                                    .localDateTime(null)
                                    .build()
                    ))
            );
        }
        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidPutTransactionDtos")
        void invalidDto_shouldThrow400(InvalidDtoCase invalidDtoCase){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(adminDto.getEmail(),adminDto.getPassword());
            when(transactionService.updateTransaction(any(TransactionDto.class))).thenReturn(transactionDto);

            ResponseEntity<TransactionDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions",
                    HttpMethod.PUT,
                    getHttpEntity(invalidDtoCase.dto(),
                            jwtResponseDto.getBody().getAccessToken()),
                    TransactionDto.class
            );

            assertEquals(HttpStatus.BAD_REQUEST, cardResponseEntity.getStatusCode());
        }
        @Test
        void admin_success(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(adminDto.getEmail(),adminDto.getPassword());
            when(transactionService.updateTransaction(any(TransactionDto.class))).thenReturn(transactionDto);

            ResponseEntity<TransactionDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions",
                    HttpMethod.PUT,
                    getHttpEntity(modelMapper.map(transactionDto, TransactionDto.class),
                            jwtResponseDto.getBody().getAccessToken()),
                    TransactionDto.class
            );

            assertEquals(HttpStatus.OK, cardResponseEntity.getStatusCode());
            assertNotNull(cardResponseEntity.getBody());
            assertEquals(transactionId, cardResponseEntity.getBody().getId());
        }
        @Test
        void sourceOwner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(sourceOwnerDto.getEmail(),sourceOwnerDto.getPassword());
            when(transactionService.updateTransaction(any(TransactionDto.class))).thenReturn(transactionDto);

            ResponseEntity<TransactionDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions",
                    HttpMethod.PUT,
                    getHttpEntity(modelMapper.map(transactionDto, TransactionDto.class),
                            jwtResponseDto.getBody().getAccessToken()),
                    TransactionDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
        @Test
        void nonOwner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(nonOwnerDto.getEmail(),nonOwnerDto.getPassword());
            when(transactionService.updateTransaction(any(TransactionDto.class))).thenReturn(transactionDto);

            ResponseEntity<TransactionDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions",
                    HttpMethod.PUT,
                    getHttpEntity(modelMapper.map(transactionDto, TransactionDto.class),
                            jwtResponseDto.getBody().getAccessToken()),
                    TransactionDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
    }

    @Nested
    class deleteTransactionById {
        @Test
        void admin_success(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(adminDto.getEmail(),adminDto.getPassword());
            doNothing().when(transactionService).deleteTransactionById(any(UUID.class));

            ResponseEntity<TransactionDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions",
                    HttpMethod.DELETE,
                    getHttpEntity(new IdDto(transactionId),
                            jwtResponseDto.getBody().getAccessToken()),
                    TransactionDto.class
            );

            assertEquals(HttpStatus.OK, cardResponseEntity.getStatusCode());
            assertNull(cardResponseEntity.getBody());
        }
        @Test
        void sourceOwner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(sourceOwnerDto.getEmail(),sourceOwnerDto.getPassword());
            doNothing().when(transactionService).deleteTransactionById(any(UUID.class));

            ResponseEntity<TransactionDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions",
                    HttpMethod.DELETE,
                    getHttpEntity(new IdDto(transactionId),
                            jwtResponseDto.getBody().getAccessToken()),
                    TransactionDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
        @Test
        void nonOwner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(nonOwnerDto.getEmail(),nonOwnerDto.getPassword());
            doNothing().when(transactionService).deleteTransactionById(any(UUID.class));

            ResponseEntity<TransactionDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions",
                    HttpMethod.DELETE,
                    getHttpEntity(new IdDto(transactionId),
                            jwtResponseDto.getBody().getAccessToken()),
                    TransactionDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
    }

    @Nested
    class makeTransaction {
        @Test
        void admin_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(adminDto.getEmail(),adminDto.getPassword());
            doNothing().when(transactionService).makeTransaction(any(UUID.class),any(UUID.class),any(BigDecimal.class));

            ResponseEntity<Void> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions/make",
                    HttpMethod.POST,
                    getHttpEntity(paymentDto,
                            jwtResponseDto.getBody().getAccessToken()),
                    Void.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
        @Test
        void sourceOwner_success(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(sourceOwnerDto.getEmail(),sourceOwnerDto.getPassword());
            doNothing().when(transactionService).makeTransaction(any(UUID.class),any(UUID.class),any(BigDecimal.class));

            ResponseEntity<Void> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions/make",
                    HttpMethod.POST,
                    getHttpEntity(paymentDto,
                            jwtResponseDto.getBody().getAccessToken()),
                    Void.class
            );

            assertEquals(HttpStatus.OK, cardResponseEntity.getStatusCode());
            assertNull(cardResponseEntity.getBody());
        }
        @Test
        void nonOwner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(nonOwnerDto.getEmail(),nonOwnerDto.getPassword());
            doNothing().when(transactionService).makeTransaction(any(UUID.class),any(UUID.class),any(BigDecimal.class));

            ResponseEntity<Void> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions/make",
                    HttpMethod.POST,
                    getHttpEntity(paymentDto,
                            jwtResponseDto.getBody().getAccessToken()),
                    Void.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
    }

    @Nested
    class findAll {
        @Test
        void admin_success(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(adminDto.getEmail(),adminDto.getPassword());
            when(transactionService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(transactionDto)));

            ResponseEntity<TransactionDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions/all",
                    HttpMethod.GET,
                    getHttpEntity(null,
                            jwtResponseDto.getBody().getAccessToken()),
                    TransactionDto.class
            );

            assertEquals(HttpStatus.OK, cardResponseEntity.getStatusCode());
            assertNotNull(cardResponseEntity.getBody());
        }
        @Test
        void nonAdmin_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(nonOwnerDto.getEmail(),nonOwnerDto.getPassword());
            when(transactionService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(transactionDto)));

            ResponseEntity<TransactionDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions/all",
                    HttpMethod.GET,
                    getHttpEntity(null,
                            jwtResponseDto.getBody().getAccessToken()),
                    TransactionDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
    }

    @Nested
    class findAllByCardId {
        @Test
        void admin_success(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(adminDto.getEmail(),adminDto.getPassword());
            when(transactionService.findAllByCardId(any(UUID.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(transactionDto)));

            ResponseEntity<TransactionDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions/all/card",
                    HttpMethod.POST,
                    getHttpEntity(new IdDto(sourceCardId),
                            jwtResponseDto.getBody().getAccessToken()),
                    TransactionDto.class
            );

            assertEquals(HttpStatus.OK, cardResponseEntity.getStatusCode());
            assertNotNull(cardResponseEntity.getBody());
        }
        @Test
        void owner_success(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(sourceOwnerDto.getEmail(),sourceOwnerDto.getPassword());
            when(transactionService.findAllByCardId(any(UUID.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(transactionDto)));

            ResponseEntity<TransactionDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions/all/card",
                    HttpMethod.POST,
                    getHttpEntity(new IdDto(sourceCardId),
                            jwtResponseDto.getBody().getAccessToken()),
                    TransactionDto.class
            );

            assertEquals(HttpStatus.OK, cardResponseEntity.getStatusCode());
            assertNotNull(cardResponseEntity.getBody());
        }
        @Test
        void nonOwner_shouldThrow403(){
            ResponseEntity<JwtResponseDto> jwtResponseDto = login(nonOwnerDto.getEmail(),nonOwnerDto.getPassword());
            when(transactionService.findAllByCardId(any(UUID.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(transactionDto)));

            ResponseEntity<TransactionDto> cardResponseEntity = restTemplate.exchange(
                    baseUrl() + "/transactions/all/card",
                    HttpMethod.POST,
                    getHttpEntity(new IdDto(sourceCardId),
                            jwtResponseDto.getBody().getAccessToken()),
                    TransactionDto.class
            );

            assertEquals(HttpStatus.FORBIDDEN, cardResponseEntity.getStatusCode());
        }
    }
}