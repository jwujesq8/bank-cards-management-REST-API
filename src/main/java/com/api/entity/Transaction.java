package com.api.entity;

import com.api.dto.CardDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@NoArgsConstructor
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_card_id", nullable = false)
    private Card sourceCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_card_id", nullable = false)
    private Card destinationCard;

    @Column(name = "local_date_time")
    private LocalDateTime localDateTime;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    public Transaction(Card sourceCard, Card destinationCard, LocalDateTime dateTime, BigDecimal amount) {
        this.sourceCard = sourceCard;
        this.destinationCard = destinationCard;
        this.localDateTime = dateTime;
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }
}
