package ru.homework.microservice.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.homework.microservice.common.AccountStatus;
import ru.homework.microservice.common.ClientStatus;
import ru.homework.microservice.dto.response.UnlockResponse;
import ru.homework.microservice.model.user.Account;
import ru.homework.microservice.model.user.Client;
import ru.homework.microservice.repository.AccountRepository;
import ru.homework.microservice.repository.ClientRepository;
import ru.homework.microservice.service.clients.UnlockClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnblockScheduler {

    private final ClientRepository clientRepo;
    private final AccountRepository accountRepo;
    private final UnlockClient unlockClient;

    @Value("${tasks.unblock.clients.count}")
    private int clientsBatchSize;
    @Value("${tasks.unblock.clients.period}")
    private long clientsPeriodMs;

    @Value("${tasks.unblock.accounts.count}")
    private int accountsBatchSize;
    @Value("${tasks.unblock.accounts.period}")
    private long accountsPeriodMs;

    @Scheduled(fixedDelayString = "${tasks.unblock.clients.period}")
    @Transactional
    public void unblockClientsTask() {
        Pageable page = PageRequest.of(0, clientsBatchSize);
        List<Client> blocked = clientRepo.findTopNByStatus(ClientStatus.BLACKLISTED, page);
        for (Client c : blocked) {
            UnlockResponse resp = unlockClient.unblockClient(c.getClientId());
            if (resp.isUnlocked()) {
                c.setStatus(ClientStatus.OK);
                clientRepo.save(c);
            }
            log.info("UnblockScheduler: client {} unlocked={}", c.getClientId(), resp.isUnlocked());
        }
    }

    @Scheduled(fixedDelayString = "${tasks.unblock.accounts.period}")
    @Transactional
    public void unblockAccountsTask() {
        Pageable page = PageRequest.of(0, accountsBatchSize);
        List<Account> arrested = accountRepo.findTopMByStatus(AccountStatus.ARRESTED, page);
        for (Account a : arrested) {
            UnlockResponse resp = unlockClient.unblockAccount(a.getAccountId());
            if (resp.isUnlocked()) {
                a.setStatus(AccountStatus.OPEN);
                accountRepo.save(a);
            }
            log.info("UnblockScheduler: account {} unlocked={}", a.getAccountId(), resp.isUnlocked());
        }
    }
}
