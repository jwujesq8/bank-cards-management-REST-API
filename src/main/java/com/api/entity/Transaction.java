package com.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Class Transaction
 *
 * Represents a financial transaction between two bank cards.
 * This entity includes details about the transaction, such as the source and destination cards,
 * the transaction amount, and the local date and time when the transaction occurred.
 */
@Entity
@Table(name = "transactions")
@NoArgsConstructor
@Getter
@Setter
public class Transaction {

    /**
     * The unique identifier of the transaction.
     * This is a UUID that uniquely identifies the transaction.
     */
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    /**
     * The source card of the transaction.
     * This is the card from which the money is being transferred.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_card_id", nullable = false)
    private Card source;

    /**
     * The destination card of the transaction.
     * This is the card to which the money is being transferred.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_card_id", nullable = false)
    private Card destination;

    /**
     * The local date and time when the transaction occurred.
     * This represents when the transaction was initiated.
     */
    @Column(name = "local_date_time")
    private LocalDateTime localDateTime;

    /**
     * The amount involved in the transaction.
     * This is the amount of money transferred from the source card to the destination card,
     * with two decimal precision.
     */
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    /**
     * Constructs a new `Transaction` instance with specified values.
     * @param source the source card for the transaction.
     * @param destination the destination card for the transaction.
     * @param dateTime the local date and time when the transaction occurred.
     * @param amount the amount of money being transferred.
     */
    public Transaction(Card source, Card destination, LocalDateTime dateTime, BigDecimal amount) {
        this.source = source;
        this.destination = destination;
        this.localDateTime = dateTime;
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Sets the amount for the transaction with two decimal precision.
     * @param amount the new amount to set for the transaction.
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }
}
