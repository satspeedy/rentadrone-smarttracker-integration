package com.hha.rentadrone.service;

import com.hha.rentadrone.domain.Delivery;
import com.hha.rentadrone.messaging.KafkaSender;
import com.hha.rentadrone.messaging.event.DeliveryStartTimeReachedEvent;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.hha.rentadrone.config.TopicNames.DELIVERY_START_TIME_REACHED;
import static com.hha.rentadrone.service.SchedulerService.JOB_DATA_MAP_DELIVERY_ID;

@Slf4j
@Component
public class DeliveryQuartzJob extends QuartzJobBean {

    private final DeliveryService deliveryService;

    private final KafkaSender kafkaSender;

    public DeliveryQuartzJob(DeliveryService deliveryService, KafkaSender kafkaSender) {
        this.deliveryService = deliveryService;
        this.kafkaSender = kafkaSender;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        log.info("Executing Job with key {}", jobExecutionContext.getJobDetail().getKey());

        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        long deliveryId = jobDataMap.getLong(JOB_DATA_MAP_DELIVERY_ID);

        Optional<Delivery> delivery = deliveryService.find(deliveryId);

        if (delivery.isEmpty()) {
            log.error("Delivery with id {} not present", deliveryId);
        } else {
            triggerDelivery(delivery.get());
        }
    }

    private void triggerDelivery(Delivery delivery) {
        DeliveryStartTimeReachedEvent deliveryStartTimeReachedEvent = mapTo(delivery);
        kafkaSender.deliveryStartTimeReached(String.valueOf(delivery.getId()), deliveryStartTimeReachedEvent, DELIVERY_START_TIME_REACHED);
        log.info("DeliveryStartTimeReached - Drone triggered with id {} for delivery with id {}", delivery.getDrone().getId(), delivery.getId());
    }

    private DeliveryStartTimeReachedEvent mapTo(Delivery entity) {
        return DeliveryStartTimeReachedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventDateTime(LocalDateTime.now())
                .deliveryId(String.valueOf(entity.getId()))
                .startAddress(entity.getStartAddress())
                .endAddress(entity.getEndAddress())
                .startLatitude(entity.getStartLatitude())
                .startLongitude(entity.getStartLongitude())
                .endLatitude(entity.getEndLatitude())
                .endLongitude(entity.getEndLongitude())
                .pickupLocalDateTime(entity.getPickupLocalDateTime())
                .estimatedTimeOfArrival(entity.getEstimatedTimeOfArrival())
                .trackingNumber(String.valueOf(entity.getTrackingNumber()))
                .deliveryStatus(entity.getDeliveryStatus().name())
                .droneId(String.valueOf(entity.getDrone().getId()))
                .droneNickName(entity.getDrone().getNickName())
                .userId(String.valueOf(entity.getUser().getId()))
                .userName(entity.getUser().getUserName())
                .build();
    }
}
