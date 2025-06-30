package ru.homework.kafka.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ru.homework.kafka.dto.TransactionMessage;
import ru.homework.kafka.dto.TransactionResultMessage;

import java.util.Map;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaListenerConfig {

    private final KafkaProperties kafkaProperties;

    @Bean("factoryIncoming")
    public ConcurrentKafkaListenerContainerFactory<String, TransactionMessage> factoryIncoming(
            ConsumerFactory<String, TransactionMessage> consumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, TransactionMessage>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, TransactionMessage> consumerFactoryIncoming() {
        return createJsonConsumerFactory(TransactionMessage.class);
    }

    @Bean("factoryResult")
    public ConcurrentKafkaListenerContainerFactory<String, TransactionResultMessage> factoryResult(
            ConsumerFactory<String, TransactionResultMessage> consumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, TransactionResultMessage>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, TransactionResultMessage> consumerFactoryResult() {
        return createJsonConsumerFactory(TransactionResultMessage.class);
    }

    private <T> DefaultKafkaConsumerFactory<String,T> createJsonConsumerFactory(Class<T> targetType) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "service1-" + targetType.getSimpleName());

        JsonDeserializer<T> deserializer = new JsonDeserializer<>(targetType);
        deserializer.addTrustedPackages("ru.homework.kafka.dto");
        deserializer.setUseTypeMapperForKey(false);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer
        );
    }

}
