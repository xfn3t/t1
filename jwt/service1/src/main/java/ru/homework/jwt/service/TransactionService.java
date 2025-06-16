package ru.homework.jwt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.homework.jwt.common.AccountStatus;
import ru.homework.jwt.common.ClientStatus;
import ru.homework.jwt.common.TransactionStatus;
import ru.homework.jwt.dto.TransactionAcceptMessage;
import ru.homework.jwt.dto.TransactionResultMessage;
import ru.homework.jwt.dto.request.TransactionRequestDto;
import ru.homework.jwt.dto.response.TransactionResponseDto;
import ru.homework.jwt.mapper.TransactionMapper;
import ru.homework.jwt.model.transaction.Transaction;
import ru.homework.jwt.model.user.Account;
import ru.homework.jwt.repository.AccountRepository;
import ru.homework.jwt.repository.TransactionRepository;
import ru.homework.jwt.service.clients.BlacklistClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final BlacklistClient blacklist;
    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;
    private final KafkaTemplate<String, TransactionAcceptMessage> acceptTpl;
    private final TransactionMapper txMapper;

    @Value("${transactions.reject.threshold}")
    private int rejectThreshold;

    @Transactional
    public TransactionResponseDto ingest(TransactionRequestDto dto) {
        Account acct = accountRepo.findByAccountId(dto.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        String clientId = acct.getClient().getClientId().toString();
        ClientStatus clientStatus = blacklist.getStatus(clientId).getStatus();

        // 1) Если клиент в чёрном списке — выставляем REJECTED или ARRESTED
        if (clientStatus == ClientStatus.BLACKLISTED) {
            return handleBlacklistedClient(acct, dto, clientId);
        }

        // 2) Клиент OK — создаём REQUESTED и шлём в Kafka
        return handleRegularTransaction(acct, dto, clientId);
    }

    private TransactionResponseDto handleBlacklistedClient(Account acct, TransactionRequestDto dto, String clientId) {
        // Подсчет отклоненных транзакций ДО создания новой
        long rejectedCount = txRepo.countByAccountAndStatus(acct, TransactionStatus.REJECTED);

        // Создаем новую отклоненную транзакцию
        Transaction tx = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .status(TransactionStatus.REJECTED)
                .timestamp(LocalDateTime.now())
                .amount(dto.getAmount())
                .account(acct)
                .build();
        txRepo.save(tx);

        // Обновляем статус счета только если он еще не арестован
        if (acct.getStatus() != AccountStatus.ARRESTED) {
            AccountStatus newStatus;

            if (rejectedCount >= rejectThreshold - 1) {  // -1 потому что текущая транзакция еще не учтена
                newStatus = AccountStatus.ARRESTED;
                log.info("🔒 Arresting account {} (rejected transactions: {}/{})",
                        acct.getAccountId(), rejectedCount + 1, rejectThreshold);
            } else {
                newStatus = AccountStatus.BLOCKED;
                log.info("⛔ Blocking account {} (rejected transactions: {}/{})",
                        acct.getAccountId(), rejectedCount + 1, rejectThreshold);
            }

            // Обновляем статус только если он изменился
            if (acct.getStatus() != newStatus) {
                acct.setStatus(newStatus);
                accountRepo.save(acct);
            }
        }

        return toResponse(tx, clientId);
    }

    private TransactionResponseDto handleRegularTransaction(Account acct, TransactionRequestDto dto, String clientId) {
        // Проверяем достаточно ли средств
        if (acct.getBalance().compareTo(dto.getAmount()) < 0) {
            log.warn("❌ Insufficient funds for account {}", acct.getAccountId());
            throw new RuntimeException("Insufficient funds");
        }

        Transaction tx = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .status(TransactionStatus.REQUESTED)
                .timestamp(LocalDateTime.now())
                .amount(dto.getAmount())
                .account(acct)
                .build();
        txRepo.save(tx);

        // Резервируем средства сразу
        acct.setBalance(acct.getBalance().subtract(dto.getAmount()));
        accountRepo.save(acct);

        // Отправляем в Kafka для дальнейшей обработки
        acceptTpl.send("t1_demo_transaction_accept",
                new TransactionAcceptMessage(
                        UUID.fromString(clientId),
                        dto.getAccountId(),
                        tx.getTransactionId(),
                        tx.getTimestamp(),
                        tx.getAmount(),
                        acct.getBalance()
                )
        );

        return toResponse(tx, clientId);
    }

    @Transactional
    public void applyResult(TransactionResultMessage msg) {
        txRepo.findByTransactionId(msg.getTransactionId())
                .ifPresent(tx -> {
                    // Сохраняем новый статус транзакции
                    tx.setStatus(msg.getStatus());

                    Account acc = tx.getAccount();
                    boolean needSave = false;

                    switch (msg.getStatus()) {
                        case REJECTED:
                            // Возвращаем средства
                            acc.setBalance(acc.getBalance().add(tx.getAmount()));
                            needSave = true;
                            log.info("🔄 Funds returned for transaction {}", tx.getTransactionId());
                            break;

                        case BLOCKED:
                            acc.setStatus(AccountStatus.BLOCKED);
                            // Замораживаем средства
                            acc.setFrozenAmount(acc.getFrozenAmount().add(tx.getAmount()));
                            needSave = true;
                            log.warn("⛔ Account {} blocked", acc.getAccountId());
                            break;

                        case ACCEPTED:
                            // Снимаем блокировку, если была
                            if (acc.getStatus() == AccountStatus.BLOCKED) {
                                acc.setStatus(AccountStatus.OPEN);
                                needSave = true;
                            }
                            log.info("✅ Transaction {} accepted", tx.getTransactionId());
                            break;

                        // Для CANCELLED и REQUESTED не требуется действий со счетом
                        default:
                            log.debug("No account state change for status: {}", msg.getStatus());
                            break;
                    }

                    if (needSave) {
                        accountRepo.save(acc);
                    }
                    txRepo.save(tx);
                });
    }

    private TransactionResponseDto toResponse(Transaction tx, String clientId) {
        return new TransactionResponseDto(
                UUID.fromString(clientId),
                tx.getAccount().getAccountId(),
                tx.getTransactionId(),
                tx.getTimestamp(),
                tx.getAmount(),
                tx.getStatus()
        );
    }

    public TransactionResponseDto getById(String txId) {
        return txRepo.findByTransactionId(txId)
                .map(txMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    public List<TransactionResponseDto> getByAccount(String accountId) {
        return txRepo.findByAccount_AccountIdOrderByTimestampDesc(accountId)
                .stream()
                .map(txMapper::toDto)
                .collect(Collectors.toList());
    }
}