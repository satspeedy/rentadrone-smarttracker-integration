package com.hha.rentadrone.web.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Transient;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;

@Data
@JsonDeserialize(builder = DeliveryDTO.DeliveryDTOBuilder.class)
@Builder(builderClassName = "DeliveryDTOBuilder", toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryDTO implements Serializable {

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

    @Positive
    @NotBlank
    @NotEmpty
    @Size(min = 4, max = 4)
    @Digits(integer=4, fraction=0)
    @Transient
    @Schema(accessMode = WRITE_ONLY)
    private String trackingPin;

    @Schema(accessMode = READ_ONLY)
    private Long trackingNumber;

    @Schema(accessMode = READ_ONLY)
    private String trackingUrl;

    @JsonPOJOBuilder(withPrefix = "")
    public static class DeliveryDTOBuilder {
    }

}
