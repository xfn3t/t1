package ru.homework.microservice.model.transaction;

import jakarta.persistence.*;
import lombok.*;
import ru.homework.microservice.common.TransactionStatus;
import ru.homework.microservice.model.user.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId = UUID.randomUUID().toString();

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(nullable = false)
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
}