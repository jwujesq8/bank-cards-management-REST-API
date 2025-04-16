package com.api.dto;

import com.api.config.enums.CardStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class CardIdStatusDto {

    @NotNull(message = "Card id can't be null")
    private UUID id;

    @NotNull(message = "New cards status can't be null")
    @Enumerated(EnumType.STRING)
    private CardStatus newStatus;

}
