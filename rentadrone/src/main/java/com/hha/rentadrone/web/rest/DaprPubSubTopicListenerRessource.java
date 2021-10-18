package com.hha.rentadrone.web.rest;

import com.hha.rentadrone.messaging.KafkaListeners;
import com.hha.rentadrone.messaging.event.DroneStatusChangedEvent;
import com.hha.rentadrone.service.StringifyHelper;
import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static com.hha.rentadrone.config.DaprConfiguration.DAPR_PUBSUB_NAME;
import static com.hha.rentadrone.config.TopicNames.DRONE_STATUS_CHANGED_TOPIC;
import static com.hha.rentadrone.config.TopicNames.TESTING_TOPIC;

@Slf4j
@RestController
public class DaprPubSubTopicListenerRessource {

    private final KafkaListeners kafkaListeners;

    public DaprPubSubTopicListenerRessource(KafkaListeners kafkaListeners) {
        this.kafkaListeners = kafkaListeners;
    }

    @Topic(name = TESTING_TOPIC, pubsubName = DAPR_PUBSUB_NAME)
    @PostMapping(path = "/" + TESTING_TOPIC)
    public ResponseEntity<String> handleTestingEvent(@RequestBody(required = false) CloudEvent<String> event) {
        logConsumedMessage(DAPR_PUBSUB_NAME, TESTING_TOPIC, event.getId(), StringifyHelper.toJson(event));
        return Optional.of(event).map(response -> ResponseEntity.ok().body(event.getData()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * Implemented redundantly handling DroneStatusChangedEvent only for demonstration purpose.
     *
     * @param event event
     * @return given event
     */
    @Topic(name = DRONE_STATUS_CHANGED_TOPIC, pubsubName = DAPR_PUBSUB_NAME)
    @PostMapping(path = "/" + DRONE_STATUS_CHANGED_TOPIC)
    public ResponseEntity<String> handleDroneStatusChangedEvent(@RequestBody(required = false) DroneStatusChangedEvent event) {
        if (event != null && event.getEventId() != null && event.getDroneId() != null) {
            logConsumedMessage(DAPR_PUBSUB_NAME, DRONE_STATUS_CHANGED_TOPIC, event.getEventId(), StringifyHelper.toJson(event));
            kafkaListeners.listenDroneStatusChangedTopic(event.getEventId(), event);
        } else {
            log.error("Event is null or doesnt contain enough data to proceed: {}", event);
        }
        return Optional.ofNullable(event).map(response -> ResponseEntity.ok().body(StringifyHelper.toJson(event)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void logConsumedMessage(String pubsubName, String topicName, String key, String message) {
        log.info("##### <- Consumed via Dapr component {} topic {} key {} with message -> {}",
                pubsubName, topicName, key, message);
    }

}
