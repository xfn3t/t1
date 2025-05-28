package ru.t1.homework.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.homework.model.Transaction;
import ru.t1.homework.service.TransactionService;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/accounts/{accountId}/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
            @PathVariable Long accountId,
            @RequestParam BigDecimal amount
    ) {
        Transaction created = transactionService.create(accountId, amount);
        return ResponseEntity
                .created(URI.create("/api/accounts/" + accountId + "/transactions/" + created.getId()))
                .body(created);
    }

    @GetMapping("/{id}")
    public Transaction getTransaction(@PathVariable Long id) {
        return transactionService.read(id);
    }

    @GetMapping
    public List<Transaction> listTransactions(@PathVariable Long accountId) {
        return transactionService.readAll(accountId);
    }

    @DeleteMapping("/{id}")
    public void deleteTransaction(@PathVariable Long id) {
        transactionService.delete(id);
    }
}
