package com.hha.rentadrone.messaging;

import com.hha.rentadrone.messaging.event.DeliveryChangedEvent;
import com.hha.rentadrone.messaging.event.DeliveryDeletedEvent;
import com.hha.rentadrone.messaging.event.DeliveryStartTimeReachedEvent;
import com.hha.rentadrone.messaging.event.DroneChangedEvent;
import com.hha.rentadrone.service.StringifyHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaSender {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTemplate<String, DroneChangedEvent> droneChangedEventKafkaTemplate;
    private final KafkaTemplate<String, DeliveryChangedEvent> deliveryChangedEventKafkaTemplate;
    private final KafkaTemplate<String, DeliveryStartTimeReachedEvent> deliveryStartTimeReachedEventKafkaTemplate;
    private final KafkaTemplate<String, DeliveryDeletedEvent> deliveryDeletedEventKafkaTemplate;

    @Autowired
    KafkaSender(KafkaTemplate<String, String> kafkaTemplate,
                KafkaTemplate<String, DroneChangedEvent> droneChangedEventKafkaTemplate,
                KafkaTemplate<String, DeliveryChangedEvent> deliveryChangedEventKafkaTemplate,
                KafkaTemplate<String, DeliveryStartTimeReachedEvent> deliveryStartTimeReachedEventKafkaTemplate, KafkaTemplate<String, DeliveryDeletedEvent> deliveryDeletedEventKafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.droneChangedEventKafkaTemplate = droneChangedEventKafkaTemplate;
        this.deliveryChangedEventKafkaTemplate = deliveryChangedEventKafkaTemplate;
        this.deliveryStartTimeReachedEventKafkaTemplate = deliveryStartTimeReachedEventKafkaTemplate;
        this.deliveryDeletedEventKafkaTemplate = deliveryDeletedEventKafkaTemplate;
    }

    public void sendMessage(String key, String message, String topicName) {
        kafkaTemplate.send(topicName, key, message);
        logProducingMessage(topicName, key, StringifyHelper.toJson(message));
    }

    public void droneChanged(String key, DroneChangedEvent message, String topicName) {
        droneChangedEventKafkaTemplate.send(topicName, key, message);
        logProducingMessage(topicName, key, StringifyHelper.toJson(message));
    }

    public void deliveryChanged(String key, DeliveryChangedEvent message, String topicName) {
        deliveryChangedEventKafkaTemplate.send(topicName, key, message);
        logProducingMessage(topicName, key, StringifyHelper.toJson(message));
    }

    public void deliveryStartTimeReached(String key, DeliveryStartTimeReachedEvent message, String topicName) {
        deliveryStartTimeReachedEventKafkaTemplate.send(topicName, key, message);
        logProducingMessage(topicName, key, StringifyHelper.toJson(message));
    }

    public void deliveryDeleted(String key, DeliveryDeletedEvent message, String topicName) {
        deliveryDeletedEventKafkaTemplate.send(topicName, key, message);
        logProducingMessage(topicName, key, StringifyHelper.toJson(message));
    }

    private void logProducingMessage(String topicName, String key, String message) {
        log.info("##### <- Published to topic {} key {} with message -> {}", topicName, key, message);
    }


}
