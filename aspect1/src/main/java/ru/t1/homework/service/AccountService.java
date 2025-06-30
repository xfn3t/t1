package ru.t1.homework.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.homework.aop.LogDataSourceError;
import ru.t1.homework.exception.DuplicateResourceException;
import ru.t1.homework.exception.ResourceNotFoundException;
import ru.t1.homework.model.Account;
import ru.t1.homework.model.Client;
import ru.t1.homework.repository.AccountRepository;

import java.util.List;
import java.util.UUID;

@Service
@LogDataSourceError
@AllArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final ClientService clientService;

    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation   = Isolation.SERIALIZABLE
    )
    public Account create(UUID clientId, Account account) {

        Client client = clientService.read(clientId);

        accountRepository.findByClient_ClientIdAndType(clientId, account.getType())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Account of this type already exists for client"
                    );
                });

        account.setClient(client);
        return accountRepository.save(account);
    }

    @Transactional(
            propagation = Propagation.SUPPORTS,
            readOnly    = true
    )
    public Account read(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    @Transactional(
            propagation = Propagation.SUPPORTS,
            readOnly    = true
    )
    public List<Account> readAll(UUID clientId) {
        return accountRepository.findByClient_ClientId(clientId);
    }

    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation   = Isolation.READ_COMMITTED
    )
    public Account update(Long accountId, Account account) {
        Account existing = read(accountId);
        existing.setType(account.getType());
        existing.setBalance(account.getBalance());
        return accountRepository.save(existing);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Account not found");
        }
        accountRepository.deleteById(accountId);
    }
}
