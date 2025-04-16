package com.api.dto;

import com.api.config.enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
