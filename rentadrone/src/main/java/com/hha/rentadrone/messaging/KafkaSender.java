package com.hha.rentadrone.messaging;

import com.hha.rentadrone.messaging.event.DeliveryChangedEvent;
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

    @Autowired
    KafkaSender(KafkaTemplate<String, String> kafkaTemplate,
                KafkaTemplate<String, DroneChangedEvent> droneChangedEventKafkaTemplate,
                KafkaTemplate<String, DeliveryChangedEvent> deliveryChangedEventKafkaTemplate,
                KafkaTemplate<String, DeliveryStartTimeReachedEvent> deliveryStartTimeReachedEventKafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.droneChangedEventKafkaTemplate = droneChangedEventKafkaTemplate;
        this.deliveryChangedEventKafkaTemplate = deliveryChangedEventKafkaTemplate;
        this.deliveryStartTimeReachedEventKafkaTemplate = deliveryStartTimeReachedEventKafkaTemplate;
    }

    public void sendMessage(String key, String message, String topicName) {
        logProducingMessage(topicName, key, StringifyHelper.toJson(message));
        kafkaTemplate.send(topicName, key, message);
    }

    public void droneChanged(String key, DroneChangedEvent message, String topicName) {
        logProducingMessage(topicName, key, StringifyHelper.toJson(message));
        droneChangedEventKafkaTemplate.send(topicName, key, message);
    }

    public void deliveryChanged(String key, DeliveryChangedEvent message, String topicName) {
        logProducingMessage(topicName, key, StringifyHelper.toJson(message));
        deliveryChangedEventKafkaTemplate.send(topicName, key, message);
    }

    public void deliveryStartTimeReached(String key, DeliveryStartTimeReachedEvent message, String topicName) {
        logProducingMessage(topicName, key, StringifyHelper.toJson(message));
        deliveryStartTimeReachedEventKafkaTemplate.send(topicName, key, message);
    }

    private void logProducingMessage(String topicName, String key, String message) {
        log.info("##### -> Producing to topic {} key {} with message -> {}", topicName, key, message);
    }


}
