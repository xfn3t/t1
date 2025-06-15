package ru.homework.kafka.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.homework.kafka.dto.TransactionAcceptMessage;
import ru.homework.kafka.dto.TransactionResultMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String servers;

    @Value("${transaction.limit.count}")
    private int limitCount;

    @Value("${transaction.limit.period}")
    private int limitPeriod;

    @Bean
    public ConsumerFactory<String, TransactionAcceptMessage> consumerAccept() {
        Map<String, Object> p = new HashMap<>();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        p.put(ConsumerConfig.GROUP_ID_CONFIG, "svc2-accept");
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        p.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        return new DefaultKafkaConsumerFactory<>(
                p,
                new StringDeserializer(),
                new JsonDeserializer<>(TransactionAcceptMessage.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransactionAcceptMessage> factoryAccept() {
        var f = new ConcurrentKafkaListenerContainerFactory<String, TransactionAcceptMessage>();
        f.setConsumerFactory(consumerAccept());
        return f;
    }

    @Bean
    public ProducerFactory<String, TransactionResultMessage> producerResult() {
        Map<String, Object> p = new HashMap<>();
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(p);
    }

    @Bean
    public KafkaTemplate<String, TransactionResultMessage> tplResult() {
        return new KafkaTemplate<>(producerResult());
    }

    @Bean
    public com.github.benmanes.caffeine.cache.Cache<String, ConcurrentLinkedQueue<TransactionAcceptMessage>> cache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(limitPeriod, TimeUnit.SECONDS)
                .build();
    }

    @Bean
    public int limitCountBean() {
        return limitCount;
    }
}
