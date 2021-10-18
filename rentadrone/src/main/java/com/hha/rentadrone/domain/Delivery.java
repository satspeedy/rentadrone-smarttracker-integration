package com.hha.rentadrone.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hha.rentadrone.domain.enumeration.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Delivery {

    private static final String DELIVERY_ID_SEQUENCE = "DELIVERY_ID_SEQ";

    @Id
    @SequenceGenerator(name = DELIVERY_ID_SEQUENCE,
            sequenceName = DELIVERY_ID_SEQUENCE,
            allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = DELIVERY_ID_SEQUENCE)
    private Long id;

    @Column(name = "start_Address", nullable = false)
    private String startAddress;

    @Column(name = "end_Address", nullable = false)
    private String endAddress;

    @Column(name = "start_Latitude")
    private Double startLatitude;

    @Column(name = "start_Longitude")
    private Double startLongitude;

    @Column(name = "end_Latitude")
    private Double endLatitude;

    @Column(name = "end_Longitude")
    private Double endLongitude;

    @Column(name = "pickup_local_date_time", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime pickupLocalDateTime;

    @Column(name = "estimated_time_of_arrival", columnDefinition = "TIMESTAMP")
    private LocalDateTime estimatedTimeOfArrival;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    private DeliveryStatus deliveryStatus;

    @OneToOne()
    @JoinColumn(name="drone_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private Drone drone;

    @OneToOne()
    @JoinColumn(name="user_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private User user;

    @Column(name = "scheduler_job_key")
    private String schedulerJobKey;

    @Column(name = "tracking_number")
    private Long trackingNumber;

    @Column(name = "tracking_url")
    private String trackingUrl;

}
