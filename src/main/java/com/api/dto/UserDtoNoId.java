package com.api.dto;

import com.api.config.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Class UserDtoNoId
 *
 * UserDtoNoId is a Data Transfer Object (DTO) representing a user without the id field.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDtoNoId {

    /**
     * The full name of the user.
     * This field must contain a non-empty value with a size between 1 and 256 characters.
     */
    @NotNull(message = "Owner must have a full name")
    @NotEmpty(message = "Full name can't be empty")
    @Size(
            min = 1,
            max = 256,
            message = "Full name acceptable  size 1-256"
    )
    @Schema(description = "User full name", example = "Name Surname")
    private String fullName;

    /**
     * The email address of the user.
     * This field must be a valid email pattern and contain a non-empty value with a size between 1 and 256 characters.
     */
    @NotNull(message = "Owner must have an email")
    @Email(message = "Provided email has wrong pattern")
    @Size(
            min = 1,
            max = 256,
            message = "Full name acceptable  size 1-256"
    )
    @Schema(description = "User email", example = "user@gmail.com")
    private String email;

    /**
     * The password of the user.
     * This field must contain a non-empty value.
     */
    @NotNull(message = "Owner must have a password")
    @NotEmpty(message = "Password can't be empty")
    @Schema(description = "User password", example = "123password")
    private String password;

    /**
     * The role assigned to the user.
     * This field must be either "USER" or "ADMIN".
     */
    @NotNull(message = "Owner must have a role")
    @Enumerated(EnumType.STRING)
    @Schema(description = "User role (USER|ADMIN)", example = "USER")
    private Role role;

}
