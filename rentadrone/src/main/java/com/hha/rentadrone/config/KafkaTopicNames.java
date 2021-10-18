package com.hha.rentadrone.config;

public class KafkaTopicNames {

    private KafkaTopicNames() {
    }

    public static final String DRONE_CHANGED_TOPIC = "drone-changed";
    public static final String DRONE_STATUS_CHANGED_TOPIC = "drone-status-changed";
    public static final String DRONE_DELETED_TOPIC = "drone-deleted";

    public static final String DELIVERY_CHANGED_TOPIC = "delivery-changed";
    public static final String DELIVERY_STATUS_CHANGED_TOPIC = "delivery-status-changed";
    public static final String DELIVERY_DELETED_TOPIC = "delivery-deleted";

    public static final String DELIVERY_START_TIME_REACHED = "delivery-start-time-reached";
}
