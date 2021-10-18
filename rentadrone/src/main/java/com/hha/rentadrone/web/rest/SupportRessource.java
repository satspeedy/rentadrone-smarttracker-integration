package com.hha.rentadrone.web.rest;

import com.hha.rentadrone.service.DeliveryService;
import com.hha.rentadrone.service.StringifyHelper;
import com.hha.rentadrone.web.rest.dto.DeliveryDTO;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.State;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import static com.hha.rentadrone.config.DaprConfiguration.DAPR_STATE_STORE_KEY_PREFIX;
import static com.hha.rentadrone.config.DaprConfiguration.DAPR_STATE_STORE_NAME;

/**
 * REST controller to serve support request.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class SupportRessource {

    private final DeliveryService deliveryService;

    private final DaprClient daprClient;

    public SupportRessource(DeliveryService deliveryService, DaprClient daprClient) {
        this.deliveryService = deliveryService;
        this.daprClient = daprClient;
    }

    /**
     * {@code GET  /deliveries/:id} : get the "id" delivery.
     *
     * @param id the id of the delivery to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the delivery, or with status {@code 404 (Not Found)}.
     */
    @SneakyThrows
    @Operation(summary = "Get a delivery by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the delivery",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeliveryDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content)})
    @GetMapping("/support/deliveries/{id}")
    public ResponseEntity<byte[]> getDelivery(@Parameter(description = "id of the delivery to retrieve") @PathVariable Long id) {
        log.info("REST request to get Delivery : {}", id);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Optional<DeliveryDTO> dto = deliveryService.findDto(id);
        if (dto.isPresent()) {
            byte[] dInBytes = StringifyHelper.toJson(dto.get()).getBytes(StandardCharsets.UTF_8);
            out.write(dInBytes);
            byte[] tInBytes = readFromStateStore(dto.get().getTrackingNumber());
            out.write(tInBytes);
        }
        return Optional.of(out.toByteArray())
                .map(response -> ResponseEntity
                        .ok()
                        .body(response))
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private byte[] readFromStateStore(Long trackingNumber) {
        log.info("Loading current state for tracking {}", trackingNumber);
        Mono<State<byte[]>> state = daprClient.getState(DAPR_STATE_STORE_NAME, DAPR_STATE_STORE_KEY_PREFIX + trackingNumber, byte[].class);
        byte[] stateValue = Objects.requireNonNull(state.block()).getValue();
        log.info("Current state of tracking {} is: {}", trackingNumber, StringifyHelper.toJson(stateValue));
        return stateValue;
    }

}
