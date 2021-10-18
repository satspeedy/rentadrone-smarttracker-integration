package com.hha.dronesim.messaging;

import com.hha.dronesim.config.KafkaTopicNames;
import com.hha.dronesim.domain.DeliveryRoute;
import com.hha.dronesim.domain.DeliveryRouteStep;
import com.hha.dronesim.domain.enumeration.DroneStatus;
import com.hha.dronesim.messaging.event.DeliveryStartTimeReachedEvent;
import com.hha.dronesim.service.DeliveryRouteService;
import com.hha.dronesim.service.StringifyHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static com.hha.dronesim.domain.DeliveryRoute.LATITUDE_EIFFEL_TOWER;
import static com.hha.dronesim.domain.DeliveryRoute.LONGITUDE_EIFFEL_TOWER;

@Slf4j
@Component
public class KafkaListeners {

    private final DeliveryRouteService deliveryRouteService;

    public KafkaListeners(DeliveryRouteService deliveryRouteService) {
        this.deliveryRouteService = deliveryRouteService;
    }

    @KafkaListener(topics = KafkaTopicNames.DELIVERY_START_TIME_REACHED_TOPIC,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "deliveryStartTimeReachedEventKafkaListenerContainerFactory")
    public void listenDeliveryStartTimeReachedTopic(
            @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
            @Payload DeliveryStartTimeReachedEvent payload) {
        logConsumedMessage(KafkaTopicNames.DELIVERY_START_TIME_REACHED_TOPIC,
                key, StringifyHelper.toJson(payload));
        DeliveryRoute deliveryRoute = mapTo(payload);
        deliveryRouteService.startDelivery(deliveryRoute);
    }

    private DeliveryRoute mapTo(DeliveryStartTimeReachedEvent e) {
        return DeliveryRoute.builder()
                .deliveryId(e.getDeliveryId())
                .startAddress(e.getStartAddress())
                .endAddress(e.getEndAddress())
                .startLatitude(e.getStartLatitude())
                .startLongitude(e.getStartLongitude())
                .endLatitude(e.getEndLatitude())
                .endLongitude(e.getEndLongitude())
                .pickupLocalDateTime(e.getPickupLocalDateTime())
                .estimatedTimeOfArrival(e.getEstimatedTimeOfArrival())
                .droneId(e.getDroneId())
                .droneNickName(e.getDroneNickName())
                .userId(e.getUserId())
                .userName(e.getUserName())
                .trackingNumber(e.getTrackingNumber())
                .currentDronePosition(DeliveryRouteStep.builder() // assumption
                        .latitude(LATITUDE_EIFFEL_TOWER)
                        .longitude(LONGITUDE_EIFFEL_TOWER)
                        .build())
                .currentDroneStatus(DroneStatus.PARKED) // assumption
                .deliveryRouteSteps(new ArrayList<>()) // initialize with empty list to avoid npe
                .triggeredByEventId(e.getEventId())
                .triggeredByEventDateTime(e.getEventDateTime())
                .build();
    }

    private void logConsumedMessage(String topicName, String key, String message) {
        log.info("##### -> Consumed at topic {} key {} with message -> {}",
                topicName, key, message);
    }

}
