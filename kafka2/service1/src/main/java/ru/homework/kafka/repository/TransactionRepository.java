package ru.homework.kafka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.homework.kafka.model.transaction.Transaction;

import java.util.List;
import java.util.Optional;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionId(String transactionId);
    List<Transaction> findByAccount_AccountIdOrderByTimestampDesc(String accountId);
}
