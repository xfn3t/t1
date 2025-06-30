package ru.homework.kafka.service.listner;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.homework.kafka.dto.TransactionMessage;
import ru.homework.kafka.dto.TransactionResultMessage;
import ru.homework.kafka.service.TransactionService;

@Component
@RequiredArgsConstructor
public class TransactionListeners {

    private final TransactionService txService;

    @KafkaListener(topics="t1_demo_transaction_result", containerFactory="factoryResult")
    public void onResult(TransactionResultMessage msg) {
        System.out.println("хоба, транзактион резуль отработал");
        txService.applyResult(msg);
    }
}
