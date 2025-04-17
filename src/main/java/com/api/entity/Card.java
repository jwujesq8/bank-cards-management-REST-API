package com.api.entity;

import com.api.config.enums.CardStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class Card
 *
 * Represents a bank card entity.
 * This entity includes details about the card, such as its unique identifier, number, owner, expiration date, status, balance,
 * transaction limit, and associated transactions (sent and received).
 */
@Entity
@Table(name = "cards")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Card {

    /**
     * The unique identifier of the card.
     * This is a UUID that uniquely identifies the card.
     */
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    /**
     * The number of the card.
     * This field stores the card number, which is typically formatted with hyphens (e.g., "1234-5678-9012-3456").
     */
    private String number;

    /**
     * The owner of the card.
     * This represents the user who owns the card.
     */
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * The expiration date of the card.
     * This field represents the date and time when the card will expire.
     */
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    /**
     * The status of the card.
     * This can be one of the following: "active", "blocked", or "expired".
     */
    @Enumerated(EnumType.STRING)
    private CardStatus status;

    /**
     * The balance available on the card.
     * This field represents the current balance of the card with two decimal precision.
     */
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal balance;

    /**
     * The transaction limit per day for the card.
     * This field represents the maximum allowed transaction amount per day.
     */
    @Column(name = "transaction_limit_per_day", precision = 10, scale = 2)
    private BigDecimal transactionLimitPerDay;

//    /**
//     * The list of transactions sent from this card.
//     * This is a one-to-many relationship with the `Transaction` entity, representing the transactions initiated by this card.
//     * @return the list of sent transactions.
//     */
//    @OneToMany(mappedBy = "source", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
//    private List<Transaction> sentTransactions;
//
//    @OneToMany(mappedBy = "destination", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
//    private List<Transaction> receivedTransactions;

    public Card(String number, User owner, LocalDateTime expirationDate,CardStatus status,
                BigDecimal balance, BigDecimal transactionLimitPerDay,
                List<Transaction> sentTransactions, List<Transaction> receivedTransactions) {
        this.number = number;
        this.owner = owner;
        this.expirationDate = expirationDate;
        this.status = status;
        this.balance = balance.setScale(2, RoundingMode.HALF_UP);;
        this.transactionLimitPerDay = transactionLimitPerDay.setScale(2, RoundingMode.HALF_UP);
//        this.sentTransactions = sentTransactions;
//        this.receivedTransactions = receivedTransactions;
    }


    public void setBalance(BigDecimal balance) {
        this.balance = balance.setScale(2, RoundingMode.HALF_UP);
    }

    public void setTransactionLimitPerDay(BigDecimal transactionLimitPerDay) {
        this.transactionLimitPerDay = transactionLimitPerDay.setScale(2, RoundingMode.HALF_UP);
    }

//    public List<Transaction> getHistory(){
//        List<Transaction> history = new ArrayList<>();
//        history.addAll(this.sentTransactions);
//        history.addAll(this.receivedTransactions);
//        return history;
//    }

}
