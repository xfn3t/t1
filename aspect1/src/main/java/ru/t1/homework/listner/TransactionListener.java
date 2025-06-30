package ru.t1.homework.listner;

import jakarta.persistence.PrePersist;
import ru.t1.homework.model.Transaction;

import java.time.Instant;

public class TransactionListener {
    @PrePersist
    public void setTimestamp(Transaction transaction) {
        transaction.setTimestamp(Instant.now());
    }
}