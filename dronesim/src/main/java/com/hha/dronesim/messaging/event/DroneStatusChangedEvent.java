package com.hha.dronesim.messaging.event;

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
@JsonDeserialize(builder = DroneStatusChangedEvent.DroneStatusChangedEventBuilder.class)
@Builder(builderClassName = "DroneStatusChangedEventBuilder", toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DroneStatusChangedEvent {

    private String eventId;

    private String droneId;

    private LocalDateTime eventDateTime;

    private String nickName;

    private String droneStatus;

    @JsonPOJOBuilder(withPrefix = "")
    public static class DroneStatusChangedEventBuilder {
    }
}
