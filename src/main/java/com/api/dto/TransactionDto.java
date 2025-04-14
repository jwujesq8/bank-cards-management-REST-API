package com.api.dto;

import com.api.entity.Card;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TransactionDto {

    @NotNull(message = "Transaction must have an id")
    private UUID id;

    @NotNull(message = "Transaction must have local date and time")
    private LocalDateTime localDateTime;

    @NotNull(message = "Transaction must have an amount")
    private BigDecimal amount;
}
