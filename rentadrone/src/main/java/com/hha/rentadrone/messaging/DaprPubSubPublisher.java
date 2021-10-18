package com.hha.rentadrone.messaging;

import io.dapr.client.DaprClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.hha.rentadrone.config.DaprConfiguration.*;
import static io.dapr.client.domain.Metadata.TTL_IN_SECONDS;

@Slf4j
@Component
public class DaprPubSubPublisher {

    private final DaprClient daprClient;

    public DaprPubSubPublisher(DaprClient daprClient) {
        this.daprClient = daprClient;
    }

    public void publishEvent(String topic, String key, String message, Map<String, String> additionalMetaData) {
        HashMap<String, String> metaData = new HashMap<>();
        metaData.put(DAPR_PUBSUB_MESSAGE_PARTITION_KEY, key);
        metaData.put(TTL_IN_SECONDS, DAPR_PUBSUB_MESSAGE_TTL_IN_SECONDS);
        metaData.putAll(additionalMetaData);
        daprClient.publishEvent(
                        DAPR_PUBSUB_NAME,
                        topic,
                        message,
                        metaData)
                .block();
        logPublishedMessage(DAPR_PUBSUB_NAME, topic, key, message);
    }

    private void logPublishedMessage(String pubsubName, String topicName, String key, String message) {
        log.info("##### <- Published via Dapr component {} topic {} key {} with message -> {}",
                pubsubName, topicName, key, message);
    }

}
