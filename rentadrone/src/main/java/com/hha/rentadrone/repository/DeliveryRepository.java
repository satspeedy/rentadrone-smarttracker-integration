package com.hha.rentadrone.repository;

import com.hha.rentadrone.domain.Delivery;
import com.hha.rentadrone.domain.enumeration.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    List<Delivery> findAllByUserId(Long userId);

    @Modifying
//    @Query("UPDATE Delivery DEL SET DEL.deliveryStatus = :deliveryStatus WHERE DEL.drone.id = :droneId AND DEL.pickupLocalDateTime <= :flightAtDesiredTime AND DEL.estimatedTimeOfArrival >= :flightAtDesiredTime")
    @Query("UPDATE Delivery DEL SET DEL.deliveryStatus = :deliveryStatus WHERE DEL.drone.id = :droneId AND :flightAtDesiredTime BETWEEN DEL.pickupLocalDateTime AND DEL.estimatedTimeOfArrival")
    void updateDeliveryStatusOfAllActiveByDroneId(@Param("deliveryStatus") DeliveryStatus deliveryStatus, @Param("droneId") Long droneId, @Param("flightAtDesiredTime") LocalDateTime flightAtDesiredTime);
}
