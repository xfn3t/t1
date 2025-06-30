package ru.t1.homework.service;

import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.homework.aop.LogDataSourceError;
import ru.t1.homework.exception.ResourceNotFoundException;
import ru.t1.homework.model.Account;
import ru.t1.homework.model.Transaction;
import ru.t1.homework.repository.TransactionRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@LogDataSourceError
@AllArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    /**
     * - валидация суммы;
     * - сохранение транзакции;
     * - обновление баланса.
     */
    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation   = Isolation.SERIALIZABLE
    )
    public Transaction create(Long accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be positive");
        }

        Account account = accountService.read(accountId);

        Transaction tx = Transaction.builder()
                .account(account)
                .amount(amount)
                .build();
        Transaction saved = transactionRepository.save(tx);

        account.setBalance(account.getBalance().add(amount));
        accountService.update(account.getId(), account);

        return saved;
    }

    /**
     * Чтение одной транзакции.
     */
    @Transactional(
            propagation = Propagation.SUPPORTS,
            readOnly    = true
    )
    public Transaction read(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Transaction not found")
                );
    }

    /**
     * Список всех транзакций по счёту.
     * Проверяем существование счёта, затем возвращаем список.
     */
    @Transactional(
            propagation = Propagation.SUPPORTS,
            readOnly    = true
    )
    public List<Transaction> readAll(Long accountId) {
        accountService.read(accountId);
        return transactionRepository.findByAccountId(accountId);
    }

    @Transactional(
            propagation = Propagation.REQUIRED
    )
    public void delete(Long transactionId) {
        if (!transactionRepository.existsById(transactionId)) {
            throw new ResourceNotFoundException("Transaction not found");
        }
        transactionRepository.deleteById(transactionId);
    }
}
