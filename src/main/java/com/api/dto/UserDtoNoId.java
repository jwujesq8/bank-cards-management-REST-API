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

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDtoNoId {

    @NotNull(message = "Owner must have a full name")
    @NotEmpty(message = "Full name can't be empty")
    @Size(
            min = 1,
            max = 256,
            message = "Full name acceptable  size 1-256"
    )
    @Schema(description = "User full name", example = "Name Surname")
    private String fullName;

    @NotNull(message = "Owner must have an email")
    @Email(message = "Provided email has wrong pattern")
    @Size(
            min = 1,
            max = 256,
            message = "Full name acceptable  size 1-256"
    )
    @Schema(description = "User email", example = "user@gmail.com")
    private String email;

    @NotNull(message = "Owner must have a password")
    @NotEmpty(message = "Password can't be empty")
    @Schema(description = "User password", example = "123password")
    private String password;

    @NotNull(message = "Owner must have a role")
    @Enumerated(EnumType.STRING)
    @Schema(description = "User role (USER|ADMIN)", example = "USER")
    private Role role;

}
