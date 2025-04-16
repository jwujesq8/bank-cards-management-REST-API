package com.api.dto;

import com.api.config.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserDto {

    @NotNull(message = "Owner must have an id")
    private UUID id;

    @NotNull(message = "Owner must have a full name")
    @NotEmpty(message = "Full name can't be empty")
    private String fullName;

    @NotNull(message = "Owner must have an email")
    @Email(message = "Provided email has wrong pattern")
    private String email;

    @NotNull(message = "Owner must have a password")
    @NotEmpty(message = "Password can't be empty")
    private String password;

    @NotNull(message = "Owner must have a role")
    @Enumerated(EnumType.STRING)
    private Role role;
}
