package ru.t1.homework;

import ru.t1.homework.exception.DuplicateResourceException;
import ru.t1.homework.exception.ResourceNotFoundException;
import ru.t1.homework.model.Account;
import ru.t1.homework.model.AccountType;
import ru.t1.homework.model.Client;
import ru.t1.homework.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.t1.homework.service.AccountService;
import ru.t1.homework.service.ClientService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private AccountService accountService;

    private Client client;
    private Account account;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);

        client = Client.builder()
                .id(1L)
                .clientId(UUID.randomUUID())
                .firstName("Ivan")
                .middleName("I.")
                .lastName("Ivanov")
                .build();

        account = Account.builder()
                .id(10L)
                .client(client)
                .type(AccountType.DEBIT)
                .balance(BigDecimal.ZERO)
                .build();
    }

    @Test
    void create_success() {

        when(clientService.read(client.getClientId()))
                .thenReturn(client);

        when(accountRepository.findByClient_ClientIdAndType(client.getClientId(), account.getType()))
                .thenReturn(Optional.empty());

        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account a = invocation.getArgument(0);
            a.setId(11L);
            return a;
        });

        Account created = accountService.create(client.getClientId(), account);

        assertEquals(client, created.getClient());
        assertEquals(AccountType.DEBIT, created.getType());

        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void create_duplicate_throws() {

        when(clientService.read(client.getClientId()))
                .thenReturn(client);

        when(accountRepository.findByClient_ClientIdAndType(client.getClientId(), account.getType()))
                .thenReturn(Optional.of(account));

        assertThrows(DuplicateResourceException.class, () ->
                accountService.create(client.getClientId(), account)
        );
    }

    @Test
    void read_found() {
        when(accountRepository.findById(account.getId()))
                .thenReturn(Optional.of(account));

        Account found = accountService.read(account.getId());
        assertEquals(account, found);
    }

    @Test
    void read_notFound_throws() {
        when(accountRepository.findById(account.getId()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                accountService.read(account.getId())
        );
    }

    @Test
    void readAll_success() {
        when(accountRepository.findByClient_ClientId(client.getClientId()))
                .thenReturn(List.of(account));

        List<Account> list = accountService.readAll(client.getClientId());
        assertEquals(1, list.size());
    }

    @Test
    void update_success() {
        when(accountRepository.findById(account.getId()))
                .thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        account.setBalance(BigDecimal.TEN);
        Account updated = accountService.update(account.getId(), account);
        assertEquals(BigDecimal.TEN, updated.getBalance());
    }

    @Test
    void delete_notFound_throws() {
        when(accountRepository.existsById(account.getId()))
                .thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () ->
                accountService.delete(account.getId())
        );
    }
}
