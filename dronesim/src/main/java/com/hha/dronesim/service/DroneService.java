package com.hha.dronesim.service;

import com.hha.dronesim.domain.enumeration.DroneStatus;
import com.hha.dronesim.messaging.KafkaSender;
import com.hha.dronesim.messaging.event.DroneStatusChangedEvent;
import com.hha.dronesim.messaging.event.TrackingPositionChangedEvent;
import com.hha.dronesim.messaging.event.enumeration.TrackingSignal;
import io.dapr.client.DaprClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.hha.dronesim.config.DaprConfiguration.DAPR_BINDING_NAME;
import static com.hha.dronesim.config.DaprConfiguration.DAPR_BINDING_OPERATION;
import static com.hha.dronesim.config.KafkaTopicNames.DRONE_STATUS_CHANGED_TOPIC;

@Slf4j
@Service
@Transactional
public class DroneService {

    private final KafkaSender kafkaSender;

    private final DaprClient daprClient;


    public DroneService(KafkaSender kafkaSender, DaprClient daprClient) {
        this.kafkaSender = kafkaSender;
        this.daprClient = daprClient;
    }

    public void publishDroneStatus(String droneId, String nickName,
                                   DroneStatus droneStatus) {
        String eventId = UUID.randomUUID().toString();
        DroneStatusChangedEvent event = DroneStatusChangedEvent.builder()
                .eventId(eventId)
                .eventDateTime(LocalDateTime.now())
                .droneId(droneId)
                .nickName(nickName)
                .droneStatus(droneStatus.name()).build();
        kafkaSender.sendDroneStatusChangedEvent(eventId,
                event,
                DRONE_STATUS_CHANGED_TOPIC);
        logPublishedMessage(DRONE_STATUS_CHANGED_TOPIC, eventId,
                StringifyHelper.toJson(event));
    }

    private void logPublishedMessage(String topicName, String key,
                                     String message) {
        log.info("##### <- Published to topic {} key {} with message -> {}",
                topicName, key, message);
    }

    public void publishDronePosition(String number, TrackingSignal signal,
                                     double latitude, double longitude) {
        String eventId = UUID.randomUUID().toString();
        TrackingPositionChangedEvent event = TrackingPositionChangedEvent.builder()
                .eventId(eventId)
                // FIXME is already added in pom but changes nothing:
                // causes Java 8 date/time type `java.time.LocalDateTime`
                // not supported by default
                // add Module "com.fasterxml.jackson.datatype:jackson-datatype-jsr310"
                // to enable handling
//                .eventDateTime(LocalDateTime.now())
                .trackingNumber(number)
                .trackingSignal(signal)
                .latitude(latitude)
                .longitude(longitude)
                .build();
        daprClient.invokeBinding(DAPR_BINDING_NAME, DAPR_BINDING_OPERATION,
                event).block();
        logOutputBindingMessageViaDapr(DAPR_BINDING_NAME, eventId,
                StringifyHelper.toJson(event));
    }

    private void logOutputBindingMessageViaDapr(String bindingName, String eventId, String event) {
        log.info("##### <- Published via Dapr Binding component {} key {} with message -> {}",
                bindingName, eventId, event);
    }

}
