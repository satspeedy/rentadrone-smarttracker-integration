package com.hha.rentadrone.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hha.rentadrone.domain.enumeration.DroneStatus;
import com.hha.rentadrone.domain.enumeration.OperationStatus;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "drones")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Drone {

    private static final String DRONE_ID_SEQUENCE = "DRONE_ID_SEQ";

    @Getter
    @Setter
    @Id
    @SequenceGenerator(name = DRONE_ID_SEQUENCE,
            sequenceName = DRONE_ID_SEQUENCE,
            allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = DRONE_ID_SEQUENCE)
    private Long id;

    @Getter
    @Setter
    @Column(name = "nick_name", nullable = false)
    private String nickName;

    @Getter
    @Setter
    @Column(name = "model", nullable = false)
    private String model;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "drone_status", nullable = false)
    private DroneStatus droneStatus;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_status")
    private OperationStatus operationStatus;

    @Getter
    @Setter
    @Lob
    @Column(name = "image")
    private byte[] image;

    @Getter
    @Setter
    @Column(name = "image_content_type")
    private String imageContentType;
}
