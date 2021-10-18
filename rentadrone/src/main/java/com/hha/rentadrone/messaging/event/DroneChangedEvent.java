package com.hha.rentadrone.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonDeserialize(builder = DroneChangedEvent.DroneChangedEventBuilder.class)
@Builder(builderClassName = "DroneChangedEventBuilder", toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DroneChangedEvent {

    private String eventId;

    private LocalDateTime eventDateTime;

    private Long droneId;

    private String nickName;

    private String model;

    private String droneStatus;

    private String operationStatus;

    private byte[] image;

    private String imageContentType;

    @JsonPOJOBuilder(withPrefix = "")
    public static class DroneChangedEventBuilder {
    }
}
