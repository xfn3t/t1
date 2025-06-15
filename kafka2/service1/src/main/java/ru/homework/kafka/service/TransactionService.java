package ru.homework.kafka.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.homework.kafka.common.AccountStatus;
import ru.homework.kafka.common.TransactionStatus;
import ru.homework.kafka.dto.TransactionAcceptMessage;
import ru.homework.kafka.dto.TransactionMessage;
import ru.homework.kafka.dto.TransactionResultMessage;
import ru.homework.kafka.dto.request.TransactionRequestDto;
import ru.homework.kafka.dto.response.TransactionResponseDto;
import ru.homework.kafka.mapper.TransactionMapper;
import ru.homework.kafka.model.transaction.Transaction;
import ru.homework.kafka.model.user.Account;
import ru.homework.kafka.repository.AccountRepository;
import ru.homework.kafka.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;
    private final KafkaTemplate<String, TransactionAcceptMessage> acceptTpl;
    private final TransactionMapper txMapper;

    @Transactional
    public TransactionResponseDto create(TransactionRequestDto dto) {
        Account acc = accountRepo.findByAccountId(dto.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        Transaction tx = txMapper.toEntity(dto);
        tx.setTransactionId(UUID.randomUUID().toString());
        tx.setStatus(TransactionStatus.REQUESTED);
        tx.setTimestamp(LocalDateTime.now());
        tx.setAccount(acc);
        txRepo.save(tx);
        // adjust balance
        acc.setBalance(acc.getBalance().subtract(dto.getAmount()));
        accountRepo.save(acc);
        // send to Kafka
        TransactionAcceptMessage am = new TransactionAcceptMessage();
        am.setClientId(acc.getClient().getClientId());
        am.setAccountId(acc.getAccountId());
        am.setTransactionId(tx.getTransactionId());
        am.setTimestamp(tx.getTimestamp());
        am.setAmount(tx.getAmount());
        am.setBalance(acc.getBalance());
        acceptTpl.send("t1_demo_transaction_accept", am);
        return txMapper.toDto(tx);
    }

    @Transactional
    public void applyResult(TransactionResultMessage msg) {
        txRepo.findByTransactionId(msg.getTransactionId())
                .ifPresent(tx -> {
                    tx.setStatus(msg.getStatus());
                    Account acc = tx.getAccount();
                    switch (msg.getStatus()) {
                        case BLOCKED:
                            acc.setStatus(ru.homework.kafka.common.AccountStatus.BLOCKED);
                            acc.setFrozenAmount(acc.getFrozenAmount().add(tx.getAmount()));
                            acc.setBalance(acc.getBalance().add(tx.getAmount()));
                            break;
                        case REJECTED:
                            acc.setBalance(acc.getBalance().add(tx.getAmount()));
                            break;
                        default:
                            break;
                    }
                    accountRepo.save(acc);
                    txRepo.save(tx);
                });
    }

    public TransactionResponseDto getById(String txId) {
        return txRepo.findByTransactionId(txId)
                .map(txMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    public List<TransactionResponseDto> getByAccount(String accountId) {
        return txRepo.findByAccount_AccountIdOrderByTimestampDesc(accountId)
                .stream().map(txMapper::toDto).collect(Collectors.toList());
    }
}