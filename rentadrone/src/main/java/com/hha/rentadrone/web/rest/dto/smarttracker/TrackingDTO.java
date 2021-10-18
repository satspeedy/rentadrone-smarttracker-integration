package com.hha.rentadrone.web.rest.dto.smarttracker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

@Data
@JsonDeserialize(builder = TrackingDTO.TrackingDTOBuilder.class)
@Builder(builderClassName = "TrackingDTOBuilder", toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackingDTO {

    private Long trackingNumber;

    private String trackingUrl;

    private String pin;

    @JsonPOJOBuilder(withPrefix = "")
    public static class TrackingDTOBuilder {
    }
}
