package com.api.exceptionHandler;

import com.api.dto.error.ErrorMessageResponseDto;
import com.api.dto.error.ValidationErrorMessageResponseDto;
import com.api.exception.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Class ExceptionControllerAdvice
 *
 * Global exception handler for handling different types of exceptions in the application.
 * This class provides centralized exception handling and returns custom error responses.
 */
@RestControllerAdvice
public class ExceptionControllerAdvice {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final Logger log = LoggerFactory.getLogger(ExceptionControllerAdvice.class);

    /**
     * Helper method to generate a standardized error response.
     *
     * @param errorMessage The error message to be included in the response.
     * @throws JsonProcessingException if the error message cannot be processed.
     */
    private ErrorMessageResponseDto getResponseBody(String errorMessage) throws JsonProcessingException {
        return ErrorMessageResponseDto.builder()
                .dateTime("UTC: " + formatter.format(Instant.now().atZone(ZoneId.of("UTC"))))
                .description(errorMessage)
                .build();
    }

    /**
     * Handles BadRequestException and returns a BAD_REQUEST response with the error message.
     *
     * @param e The BadRequestException to be handled.
     * @return A ResponseEntity with a custom error message and a BAD_REQUEST status.
     * @throws JsonProcessingException if the error message cannot be processed.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorMessageResponseDto> badRequestExceptionHandler(BadRequestException e)
            throws JsonProcessingException {
        log.error("Exception: BadRequestException. " +
                "Exception message: " + e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(getResponseBody(e.getMessage()));
    }

    /**
     * Handles ForbiddenException and returns an FORBIDDEN response with the error message.
     *
     * @param e The ForbiddenException to be handled.
     * @return A ResponseEntity with a custom error message and an FORBIDDEN status.
     * @throws JsonProcessingException if the error message cannot be processed.
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorMessageResponseDto> authExceptionHandler(ForbiddenException e)
            throws JsonProcessingException {
        log.error("Exception: ForbiddenException. " +
                "Exception message: " + e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(getResponseBody(e.getMessage()));
    }

    /**
     * Handles OkException and returns an OK response with the error message.
     *
     * @param e The OkException to be handled.
     * @return A ResponseEntity with a custom error message and an OK status.
     * @throws JsonProcessingException if the error message cannot be processed.
     */
    @ExceptionHandler(OkException.class)
    public ResponseEntity<ErrorMessageResponseDto> okExceptionHandler(OkException e) throws JsonProcessingException{
        log.error("Exception: OkException. " +
                "Exception message: " + e.getMessage());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(getResponseBody(e.getMessage()));
    }

    /**
     * Handles ServerException and returns an INTERNAL_SERVER_ERROR response with the error message.
     *
     * @param e The ServerException to be handled.
     * @return A ResponseEntity with a custom error message and an INTERNAL_SERVER_ERROR status.
     * @throws JsonProcessingException if the error message cannot be processed.
     */
    @ExceptionHandler(ServerException.class)
    public ResponseEntity<ErrorMessageResponseDto> serverExceptionHandler(ServerException e) throws JsonProcessingException{
        log.error("Exception: ServerException. " +
                "Exception message: " + e.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(getResponseBody(e.getMessage()));
    }

    /**
     * Handles HttpMessageNotReadableException which occurs when the request body is not readable or is invalid.
     * This is typically thrown when the request body cannot be parsed into the expected Java object.
     * The exception is logged, and an appropriate error message is returned to the client.
     *
     * @param e The exception that was thrown, containing details about the error.
     * @return A `ResponseEntity` containing an `ErrorMessageResponseDto` with the error details.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMessageResponseDto> handleHttpMessageNotReadable(HttpMessageNotReadableException e)
            throws JsonProcessingException {
        log.error("Exception: HttpMessageNotReadableException. " +
                "Exception message: " + e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(getResponseBody("Invalid request body: " + e.getMostSpecificCause().getMessage()));
    }

    /**
     * Handles ValidException and returns a BAD_REQUEST response with the error message.
     *
     * @param e The ValidException to be handled.
     * @return A ResponseEntity with a custom error message and a BAD_REQUEST status.
     * @throws JsonProcessingException if the error message cannot be processed.
     */
    @ExceptionHandler(ValidException.class)
    public ResponseEntity<ErrorMessageResponseDto> validationExceptionHandler(
            ValidException e) throws JsonProcessingException{
        log.error("Exception: ValidException. " +
                "Exception message: " + e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(getResponseBody(e.getMessage()));
    }

    /**
     * Handles DataIntegrityViolationException which occurs when a database integrity constraint is violated,
     * such as a foreign key or unique constraint.

     * @param ex the DataIntegrityViolationException thrown during request processing
     * @return a ResponseEntity containing an ErrorMessageResponseDto and HTTP status {@code 409 Conflict}
     * @throws JsonProcessingException if an error occurs while serializing the error message response
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorMessageResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException ex)
            throws JsonProcessingException {
        log.error("Exception: DataIntegrityViolationException. " +
                "Exception message: " + ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(getResponseBody(ex.getMessage()));
    }

    /**
     * Handles MethodArgumentNotValidException and returns a BAD_REQUEST response with a validation error message.
     * This is triggered for invalid method arguments (e.g., invalid field values).
     *
     * @param e The MethodArgumentNotValidException to be handled.
     * @return A ResponseEntity with a validation error message and a BAD_REQUEST status.
     * @throws JsonProcessingException if the validation errors cannot be processed.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorMessageResponseDto> validationExceptionHandler(
            MethodArgumentNotValidException e) throws JsonProcessingException {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName;
            String errorMessage = error.getDefaultMessage();

            if (error instanceof FieldError) {
                fieldName = ((FieldError) error).getField();
            } else {
                fieldName = error.getObjectName();
            }
            errors.put(fieldName, errorMessage);
        });
        ValidationErrorMessageResponseDto validErrorMessageResponseDto = ValidationErrorMessageResponseDto.builder()
                .dateTime("UTC: " + formatter.format(Instant.now().atZone(ZoneId.of("UTC"))))
                .errorsMap(errors)
                .build();
        log.error("Exception: MethodArgumentNotValidException. " +
                "Exception message: " + errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(validErrorMessageResponseDto);
    }
}