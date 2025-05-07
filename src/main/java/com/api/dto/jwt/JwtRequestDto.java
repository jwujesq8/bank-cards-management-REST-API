package com.api.dto.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Class JwtRequestDto
 *
 * Data Transfer Object (DTO) representing a JWT request.
 * This DTO contains user login details used for authentication.
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class JwtRequestDto {

    /**
     * The user's email address, which must be a valid email and non-empty.
     * The email length should be between 11 and 50 characters.
     */
    @NotEmpty(message = "Email can't be empty or null")
    @Size(
            max = 50,
            min = 11,
            message = "Non valid login"
    )
    @Email(message = "Entered non email")
    @Schema(description = "user email", example = "iii@gmail.com", minLength = 11, maxLength = 50)
    private String email;

    /**
     * The user's password, which must be between 7 and 20 characters long and non-empty.
     */
    @NotEmpty(message = "Password can't be empty or null")
    @Size(
            min = 7,
            max = 20,
            message = "Non valid password"
    )
    @Schema(description = "user password", example = "qqq_111", minLength = 7, maxLength = 20)
    private String password;
}
