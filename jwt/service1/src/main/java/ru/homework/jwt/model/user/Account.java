package ru.homework.jwt.model.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.homework.jwt.common.AccountStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", unique = true, nullable = false)
    private String accountId = "acc_" + UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.OPEN;

    private BigDecimal balance;

    @Column(name = "frozen_amount")
    private BigDecimal frozenAmount = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
}