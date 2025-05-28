// TransactionServiceTest.java
package ru.t1.homework;

import ru.t1.homework.exception.ResourceNotFoundException;
import ru.t1.homework.model.Account;
import ru.t1.homework.model.Transaction;
import ru.t1.homework.model.AccountType;
import ru.t1.homework.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import jakarta.validation.ValidationException;
import ru.t1.homework.service.AccountService;
import ru.t1.homework.service.TransactionService;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountService accountService;
    @InjectMocks private TransactionService transactionService;

    private Account account;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        account = Account.builder()
                .id(5L)
                .client(null)
                .type(AccountType.DEBIT)
                .balance(BigDecimal.ZERO)
                .build();
    }

    @Test
    void create_positiveAmount_success() {
        when(accountService.read(account.getId()))
                .thenReturn(account);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> {
                    Transaction t = inv.getArgument(0);
                    t.setId(100L);
                    return t;
                });
        // accountService.update() вызывается внутри create()
        doReturn(account).when(accountService).update(eq(account.getId()), any(Account.class));

        Transaction tx = transactionService.create(account.getId(), BigDecimal.valueOf(50));
        assertEquals(account.getId(), tx.getAccount().getId());
        assertEquals(BigDecimal.valueOf(50), tx.getAmount());
        verify(accountService).update(account.getId(), account);
    }

    @Test
    void create_zeroAmount_throws() {
        assertThrows(ValidationException.class, () ->
                transactionService.create(account.getId(), BigDecimal.ZERO)
        );
    }

    @Test
    void create_accountNotFound_throws() {
        when(accountService.read(account.getId()))
                .thenThrow(new ResourceNotFoundException("Account not found"));

        assertThrows(ResourceNotFoundException.class, () ->
                transactionService.create(account.getId(), BigDecimal.ONE)
        );
    }

    @Test
    void read_notFound_throws() {
        when(transactionRepository.findById(1L))
                .thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () ->
                transactionService.read(1L)
        );
    }
}
