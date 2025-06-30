package ru.homework.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.homework.jwt.common.TransactionStatus;
import ru.homework.jwt.model.transaction.Transaction;
import ru.homework.jwt.model.user.Account;

import java.util.List;
import java.util.Optional;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionId(String transactionId);
    List<Transaction> findByAccount_AccountIdOrderByTimestampDesc(String accountId);

    long countByAccount_AccountIdAndStatus(String accountId, TransactionStatus transactionStatus);

    long countByAccount_AndStatus(Account acct, TransactionStatus transactionStatus);

    long countByAccountAndStatus(Account acct, TransactionStatus transactionStatus);
}
