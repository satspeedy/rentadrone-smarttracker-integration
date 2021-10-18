package com.hha.dronesim.messaging;

import com.hha.dronesim.messaging.event.DroneStatusChangedEvent;
import com.hha.dronesim.service.StringifyHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaSender {

    private final KafkaTemplate<String, DroneStatusChangedEvent> droneStatusChangedEventKafkaTemplate;

    @Autowired
    KafkaSender(KafkaTemplate<String, DroneStatusChangedEvent> droneStatusChangedEventKafkaTemplate) {
        this.droneStatusChangedEventKafkaTemplate = droneStatusChangedEventKafkaTemplate;
    }

    public void sendDroneStatusChangedEvent(String key,
                                            DroneStatusChangedEvent message,
                                            String topicName) {
        droneStatusChangedEventKafkaTemplate.send(topicName, key, message);
        logProducingMessage(topicName, key, StringifyHelper.toJson(message));
    }

    private void logProducingMessage(String topicName, String key,
                                     String message) {
        log.info("##### <- Published to topic {} key {} with message -> {}",
                topicName, key, message);
    }

}
