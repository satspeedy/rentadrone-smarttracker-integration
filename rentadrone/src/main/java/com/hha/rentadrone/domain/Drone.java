package com.hha.rentadrone.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hha.rentadrone.domain.enumeration.DroneStatus;
import com.hha.rentadrone.domain.enumeration.OperationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "drones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Drone {

    private static final String DRONE_ID_SEQUENCE = "DRONE_ID_SEQ";

    @Id
    @SequenceGenerator(name = DRONE_ID_SEQUENCE,
            sequenceName = DRONE_ID_SEQUENCE,
            allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = DRONE_ID_SEQUENCE)
    private Long id;

    @Column(name = "nick_name", nullable = false)
    private String nickName;

    @Column(name = "model", nullable = false)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "drone_status", nullable = false)
    private DroneStatus droneStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_status")
    private OperationStatus operationStatus;

    @Lob
    @Column(name = "image")
    private byte[] image;

    @Column(name = "image_content_type")
    private String imageContentType;
}
