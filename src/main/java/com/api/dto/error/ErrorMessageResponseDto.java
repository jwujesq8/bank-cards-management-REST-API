package com.api.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Class ErrorMessageResponseDto
 *
 * Data Transfer Object (DTO) representing an error message response.
 * This class is used to provide a standardized response format for errors.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorMessageResponseDto {

    /**
     * The date and time when the error occurred.
     * This should be formatted as "dd.MM.yy HH:mm:ss".
     */
    @Schema(description = "Date and time", example = "08.08.08 15:18:13")
    private String dateTime;

    /**
     * A description of the error or status message.
     * This field cannot be blank.
     *
     * @minLength 0
     */
    @NotBlank(message = "Description must not be blank")
    @Schema(description = "response body message (fail|success etc)",
            example = "Summary of successes and issues", minLength = 0)
    private String description;

}