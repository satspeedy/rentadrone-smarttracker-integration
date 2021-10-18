package com.hha.dronesim.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hha.dronesim.domain.enumeration.DroneStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryRoute {

    public static final double LATITUDE_EIFFEL_TOWER = 48.857950;
    public static final double LONGITUDE_EIFFEL_TOWER = 2.295390;

    private String deliveryId;

    private String startAddress;

    private String endAddress;

    private double startLatitude;

    private double startLongitude;

    private double endLatitude;

    private double endLongitude;

    private LocalDateTime pickupLocalDateTime;

    private LocalDateTime estimatedTimeOfArrival;

    private String droneId;

    private String droneNickName;

    private String userId;

    private String userName;

    private String trackingNumber;

    private List<DeliveryRouteStep> deliveryRouteSteps;

    private DeliveryRouteStep currentDronePosition;

    private DroneStatus currentDroneStatus;

    private String triggeredByEventId;

    private LocalDateTime triggeredByEventDateTime;

}
