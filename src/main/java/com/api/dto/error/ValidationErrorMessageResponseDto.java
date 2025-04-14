package com.api.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

/**
 * Class ValidationErrorMessageResponseDto
 *
 * Data Transfer Object (DTO) representing a validation error message response.
 * This class is used to standardize the response format for validation errors.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidationErrorMessageResponseDto {

    /**
     * The date and time when the validation error occurred.
     * This should be formatted as "dd.MM.yy HH:mm:ss".
     */
    @Schema(description = "Date and time", example = "08.08.08 15:18:13")
    private String dateTime;

    /**
     * A map containing the field names as keys and their respective error descriptions as values.
     * This field cannot be null and should contain validation error details.
     *
     * @NotNull message = "Errors map must not be empty"
     */
    @Schema(description = "map of the field and errors description",
            example = "{\"id\": \"non valid id\"}")
    @NotNull(message = "Errors map must not be empty")
    private Map<String, String> errorsMap;
}
