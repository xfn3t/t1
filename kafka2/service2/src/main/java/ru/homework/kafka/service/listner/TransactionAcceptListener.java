package ru.homework.kafka.service.listner;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.homework.kafka.dto.TransactionAcceptMessage;
import ru.homework.kafka.service.TransactionProcessor;

@Component
@RequiredArgsConstructor
public class TransactionAcceptListener {

    private final TransactionProcessor processor;

    @KafkaListener(topics="t1_demo_transaction_accept",containerFactory="factoryAccept")
    public void listen(TransactionAcceptMessage msg) {
        System.out.println("ТРАНЗАКТИОН АССЕПТ");
        processor.process(msg);
    }
}
