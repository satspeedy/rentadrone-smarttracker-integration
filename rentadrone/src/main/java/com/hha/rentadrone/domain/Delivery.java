package com.hha.rentadrone.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hha.rentadrone.domain.enumeration.DeliveryStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Delivery {

    private static final String DELIVERY_ID_SEQUENCE = "DELIVERY_ID_SEQ";

    @Getter
    @Setter
    @Id
    @SequenceGenerator(name = DELIVERY_ID_SEQUENCE,
            sequenceName = DELIVERY_ID_SEQUENCE,
            allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = DELIVERY_ID_SEQUENCE)
    private Long id;

    @Getter
    @Setter
    @Column(name = "start_Address", nullable = false)
    private String startAddress;

    @Getter
    @Setter
    @Column(name = "end_Address", nullable = false)
    private String endAddress;

    @Getter
    @Setter
    @Column(name = "start_Latitude")
    private Double startLatitude;

    @Getter
    @Setter
    @Column(name = "start_Longitude")
    private Double startLongitude;

    @Getter
    @Setter
    @Column(name = "end_Latitude")
    private Double endLatitude;

    @Getter
    @Setter
    @Column(name = "end_Longitude")
    private Double endLongitude;

    @Getter
    @Setter
    @Column(name = "pickup_local_date_time", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime pickupLocalDateTime;

    @Getter
    @Setter
    @Column(name = "estimated_time_of_arrival", columnDefinition = "TIMESTAMP")
    private LocalDateTime estimatedTimeOfArrival;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    private DeliveryStatus deliveryStatus;

    @Getter
    @Setter
    @OneToOne()
    @JoinColumn(name="drone_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private Drone drone;

    @Getter
    @Setter
    @OneToOne()
    @JoinColumn(name="user_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private User user;

    @Getter
    @Setter
    @Column(name = "scheduler_job_key")
    private String schedulerJobKey;

}
