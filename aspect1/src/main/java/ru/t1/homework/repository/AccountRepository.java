package ru.t1.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.t1.homework.model.Account;
import ru.t1.homework.model.AccountType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByClient_ClientId(UUID clientId);
    Optional<Account> findByClientIdAndType(Long clientId, AccountType type);
    Optional<Account> findByClient_ClientIdAndType(UUID clientId, AccountType type);
}
