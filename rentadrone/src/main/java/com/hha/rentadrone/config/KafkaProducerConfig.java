package com.hha.rentadrone.config;

import com.hha.rentadrone.messaging.event.DeliveryChangedEvent;
import com.hha.rentadrone.messaging.event.DeliveryDeletedEvent;
import com.hha.rentadrone.messaging.event.DeliveryStartTimeReachedEvent;
import com.hha.rentadrone.messaging.event.DroneChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<String, DroneChangedEvent> droneChangedEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, DroneChangedEvent> droneChangedEventKafkaTemplate(ProducerFactory<String, DroneChangedEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory,
                Collections.singletonMap(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class));
    }

    @Bean
    public ProducerFactory<String, DeliveryChangedEvent> deliveryChangedEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, DeliveryChangedEvent> deliveryChangedEventKafkaTemplate(ProducerFactory<String, DeliveryChangedEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory,
                Collections.singletonMap(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class));
    }

    @Bean
    public ProducerFactory<String, DeliveryStartTimeReachedEvent> deliveryStartTimeReachedEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, DeliveryStartTimeReachedEvent> deliveryStartTimeReachedEventKafkaTemplate(ProducerFactory<String, DeliveryStartTimeReachedEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory,
                Collections.singletonMap(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class));
    }

    @Bean
    public ProducerFactory<String, DeliveryDeletedEvent> deliveryDeletedEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, DeliveryDeletedEvent> deliveryDeletedEventKafkaTemplate(ProducerFactory<String, DeliveryDeletedEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory,
                Collections.singletonMap(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class));
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

}
