package ru.homework.kafka.service;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.homework.kafka.common.TransactionStatus;
import ru.homework.kafka.dto.TransactionAcceptMessage;
import ru.homework.kafka.dto.TransactionResultMessage;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
public class TransactionProcessor {

    private final Cache<String,ConcurrentLinkedQueue<TransactionAcceptMessage>> cache;
    private final KafkaTemplate<String, TransactionResultMessage> tpl;
    private final int limitCount;

    public void process(TransactionAcceptMessage msg) {

        String key = msg.getClientId() + ":" + msg.getAccountId();

        ConcurrentLinkedQueue<TransactionAcceptMessage> q = cache.get(key,k->new ConcurrentLinkedQueue<>());
        Objects.requireNonNull(q).add(msg);

        TransactionStatus status;
        if (q.size() > limitCount) status = TransactionStatus.BLOCKED;
        else if (msg.getAmount().compareTo(msg.getBalance()) > 0)
            status = TransactionStatus.REJECTED;
        else
            status = TransactionStatus.ACCEPTED;

        if (status == TransactionStatus.BLOCKED) cache.invalidate(key);

        TransactionResultMessage out = new TransactionResultMessage();
        out.setTransactionId(msg.getTransactionId());
        out.setAccountId(msg.getAccountId());
        out.setStatus(status);

        tpl.send("t1_demo_transaction_result",out);
    }
}