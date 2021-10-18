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
@JsonDeserialize(builder = DeliveryChangedEvent.DeliveryChangedEventBuilder.class)
@Builder(builderClassName = "DeliveryChangedEventBuilder", toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryChangedEvent {

    private String eventId;

    private LocalDateTime eventDateTime;

    private Long deliveryId;

    private String startAddress;

    private String endAddress;

    private Double startLatitude;

    private Double startLongitude;

    private Double endLatitude;

    private Double endLongitude;

    private LocalDateTime pickupLocalDateTime;

    private LocalDateTime estimatedTimeOfArrival;

    private String deliveryStatus;

    private Long droneId;

    private String droneNickName;

    private Long userId;

    private String userName;

    @JsonPOJOBuilder(withPrefix = "")
    public static class DeliveryChangedEventBuilder {
    }

}
