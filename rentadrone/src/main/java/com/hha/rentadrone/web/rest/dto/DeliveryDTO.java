package com.hha.rentadrone.web.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonDeserialize(builder = DeliveryDTO.DeliveryDTOBuilder.class)
@Builder(builderClassName = "DeliveryDTOBuilder", toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryDTO {

    private Long id;

    private String startAddress;

    private String endAddress;

    private LocalDateTime pickupLocalDateTime;

    @Schema(hidden = true)
    private LocalDateTime estimatedTimeOfArrival;

    private String deliveryStatus;

    private Long droneId;

    private String userName;

    @JsonIgnore
    private String schedulerJobKey;

    @JsonPOJOBuilder(withPrefix = "")
    public static class DeliveryDTOBuilder {
    }

}
