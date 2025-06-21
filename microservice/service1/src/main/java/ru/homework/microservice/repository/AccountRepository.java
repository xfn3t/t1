package ru.homework.microservice.repository;

import feign.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.homework.microservice.common.AccountStatus;
import ru.homework.microservice.model.user.Account;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountId(String accountId);

    long countByStatus(AccountStatus status);

    @Query("select a from Account a where a.status = :status")
    List<Account> findTopMByStatus(@Param("status") AccountStatus status, Pageable pageable);
}
