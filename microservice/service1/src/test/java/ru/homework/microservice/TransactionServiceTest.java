package ru.homework.microservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ru.homework.microservice.common.AccountStatus;
import ru.homework.microservice.common.ClientStatus;
import ru.homework.microservice.common.TransactionStatus;
import ru.homework.microservice.dto.TransactionResultMessage;
import ru.homework.microservice.dto.request.TransactionRequestDto;
import ru.homework.microservice.dto.response.ClientStatusResponse;
import ru.homework.microservice.dto.response.TransactionResponseDto;
import ru.homework.microservice.model.user.Account;
import ru.homework.microservice.model.user.Client;
import ru.homework.microservice.model.transaction.Transaction;
import ru.homework.microservice.repository.AccountRepository;
import ru.homework.microservice.repository.ClientRepository;
import ru.homework.microservice.repository.TransactionRepository;
import ru.homework.microservice.service.TransactionService;
import ru.homework.microservice.service.clients.BlacklistClient;

class TransactionServiceTest {
    @Mock
    private BlacklistClient blacklistClient;
    @Mock
    private AccountRepository accountRepo;
    @Mock
    private TransactionRepository txRepo;
    @Mock
    private ClientRepository clientRepo;

    @InjectMocks
    private TransactionService service;

    private Account account;
    private Client client;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        client = Client.builder()
                .id(1L)
                .clientId(UUID.randomUUID())
                .status(ClientStatus.OK)
                .build();
        account = new Account();
        account.setAccountId("acc-123");
        account.setBalance(new BigDecimal("1000"));
        account.setStatus(AccountStatus.OPEN);
        account.setClient(client);
    }

    @Test
    void testRegularTransaction_success() {

        when(accountRepo.findByAccountId("acc-123")).thenReturn(Optional.of(account));
        when(blacklistClient.getStatus(client.getClientId().toString()))
                .thenReturn(new ru.homework.microservice.dto.response.ClientStatusResponse(client.getClientId().toString(), ClientStatus.OK));

        var dto = new TransactionRequestDto();
        dto.setAccountId("acc-123");
        dto.setAmount(new BigDecimal("200"));

        TransactionResponseDto resp = service.ingest(dto);

        assertEquals(TransactionStatus.REQUESTED, resp.getStatus());
        assertEquals(new BigDecimal("800"), account.getBalance());
        verify(txRepo).save(any(Transaction.class));
        verify(accountRepo).save(account);
    }

    @Test
    void testRegularTransaction_insufficientFunds() {
        when(accountRepo.findByAccountId("acc-123")).thenReturn(Optional.of(account));
        when(blacklistClient.getStatus(client.getClientId().toString()))
                .thenReturn(new ru.homework.microservice.dto.response.ClientStatusResponse(client.getClientId().toString(), ClientStatus.OK));

        var dto = new TransactionRequestDto();
        dto.setAccountId("acc-123");
        dto.setAmount(new BigDecimal("2000"));

        assertThrows(RuntimeException.class, () -> service.ingest(dto));
        verify(txRepo, never()).save(any());
    }

    @Test
    void testBlacklistedClient_transactionRejectedAndAccountStatusUpdated() throws IllegalAccessException, NoSuchFieldException {
        // Set threshold to 5 (requires reflection)
        Field rejectThresholdField = TransactionService.class.getDeclaredField("rejectThreshold");
        rejectThresholdField.setAccessible(true);
        rejectThresholdField.setInt(service, 5);

        when(accountRepo.findByAccountId("acc-123")).thenReturn(Optional.of(account));
        when(blacklistClient.getStatus(client.getClientId().toString()))
                .thenReturn(new ClientStatusResponse(client.getClientId().toString(), ClientStatus.BLACKLISTED));

        when(txRepo.countByAccountAndStatus(account, TransactionStatus.REJECTED)).thenReturn(0L);

        TransactionRequestDto dto = new TransactionRequestDto();
        dto.setAccountId("acc-123");
        dto.setAmount(new BigDecimal("50"));

        TransactionResponseDto resp = service.ingest(dto);

        assertEquals(TransactionStatus.REJECTED, resp.getStatus());
        assertEquals(AccountStatus.BLOCKED, account.getStatus());
        verify(txRepo).save(any(Transaction.class));
        verify(accountRepo).save(account);
    }


    @Test
    void testApplyResult_variousStatuses() {
        Transaction tx = new Transaction();
        tx.setTransactionId("tx-1");
        tx.setAmount(new BigDecimal("100"));
        tx.setAccount(account);

        when(txRepo.findByTransactionId("tx-1")).thenReturn(Optional.of(tx));

        // REJECTED
        service.applyResult(new TransactionResultMessage("tx-1", "acc-123", TransactionStatus.REJECTED));
        assertEquals(new BigDecimal("1100"), account.getBalance());

        // BLOCKED
        account.setFrozenAmount(BigDecimal.ZERO);
        service.applyResult(new TransactionResultMessage("tx-1", "acc-123", TransactionStatus.BLOCKED));
        assertEquals(AccountStatus.BLOCKED, account.getStatus());
        assertEquals(new BigDecimal("100"), account.getFrozenAmount());

        // ACCEPTED
        account.setStatus(AccountStatus.BLOCKED);
        service.applyResult(new TransactionResultMessage("tx-1", "acc-123", TransactionStatus.ACCEPTED));
        assertEquals(AccountStatus.OPEN, account.getStatus());

        verify(txRepo, times(3)).save(tx);
        verify(accountRepo, atLeastOnce()).save(account);
    }
}
