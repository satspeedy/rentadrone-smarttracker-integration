package com.hha.rentadrone.messaging;

import com.hha.rentadrone.config.TopicNames;
import com.hha.rentadrone.domain.Drone;
import com.hha.rentadrone.domain.enumeration.DroneStatus;
import com.hha.rentadrone.messaging.event.DeliveryChangedEvent;
import com.hha.rentadrone.messaging.event.DeliveryDeletedEvent;
import com.hha.rentadrone.messaging.event.DroneChangedEvent;
import com.hha.rentadrone.messaging.event.DroneStatusChangedEvent;
import com.hha.rentadrone.service.DeliveryService;
import com.hha.rentadrone.service.DroneService;
import com.hha.rentadrone.service.StringifyHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaListeners {

    private final DroneService droneService;

    private final DeliveryService deliveryService;

    public KafkaListeners(DroneService droneService, DeliveryService deliveryService) {
        this.droneService = droneService;
        this.deliveryService = deliveryService;
    }

    @KafkaListener(topics = TopicNames.DRONE_CHANGED_TOPIC, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "droneChangedEventKafkaListenerContainerFactory")
    public void listenDroneChangedTopic(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload DroneChangedEvent payload) {
        logConsumedMessage(TopicNames.DRONE_CHANGED_TOPIC, key, StringifyHelper.toJson(payload));
    }

    // Comment out to consume only via Dapr - testimonial.
//    @KafkaListener(topics = TopicNames.DRONE_STATUS_CHANGED_TOPIC, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "droneStatusChangedEventKafkaListenerContainerFactory")
    public void listenDroneStatusChangedTopic(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload DroneStatusChangedEvent payload) {
        logConsumedMessage(TopicNames.DRONE_STATUS_CHANGED_TOPIC, key, StringifyHelper.toJson(payload));
        Drone drone = mapTo(payload);
        droneService.partialUpdate(drone);
        deliveryService.updateDeliveryStatusByDroneStatus(drone.getId(), drone.getDroneStatus(), payload.getEventDateTime());
    }

    private Drone mapTo(DroneStatusChangedEvent payload) {
        Drone drone = new Drone();
        drone.setId(Long.valueOf(payload.getDroneId()));
        drone.setDroneStatus(DroneStatus.valueOf(payload.getDroneStatus()));
        return drone;
    }

    @KafkaListener(topics = TopicNames.DRONE_DELETED_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void listenDroneDeletedTopic(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload String payload) {
        logConsumedMessage(TopicNames.DRONE_DELETED_TOPIC, key, StringifyHelper.toJson(payload));
    }

    @KafkaListener(topics = TopicNames.DELIVERY_CHANGED_TOPIC, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "deliveryChangedEventKafkaListenerContainerFactory")
    public void listenDeliveryChangedTopic(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload DeliveryChangedEvent payload) {
        logConsumedMessage(TopicNames.DELIVERY_CHANGED_TOPIC, key, StringifyHelper.toJson(payload));
    }

    @KafkaListener(topics = TopicNames.DELIVERY_DELETED_TOPIC, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "deliveryDeletedEventKafkaListenerContainerFactory")
    public void listenDeliveryDeletedTopic(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload DeliveryDeletedEvent payload) {
        logConsumedMessage(TopicNames.DELIVERY_DELETED_TOPIC, key, StringifyHelper.toJson(payload));
    }

    private void logConsumedMessage(String topicName, String key, String message) {
        log.info("##### -> Consumed at topic {} key {} with message -> {}", topicName, key, message);
    }
}
