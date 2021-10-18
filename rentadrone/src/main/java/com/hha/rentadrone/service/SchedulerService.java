package com.hha.rentadrone.service;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;


@Slf4j
@Component
public class SchedulerService {

    public static final String JOB_DATA_MAP_DELIVERY_ID = "deliveryId";

    private static final String JOB_GROUP_IDENTITY_DELIVERY_JOBS = "delivery-jobs";

    private final Scheduler scheduler;

    public SchedulerService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public JobDetail buildJobDetail(Long scheduleDeliveryId) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(JOB_DATA_MAP_DELIVERY_ID, scheduleDeliveryId);

        return JobBuilder.newJob(DeliveryQuartzJob.class)
//                .withIdentity(UUID.nameUUIDFromBytes(IDENTITY_DELIVERY_JOBS.getBytes()).toString(), IDENTITY_DELIVERY_JOBS)
                .withIdentity(UUID.randomUUID().toString(), JOB_GROUP_IDENTITY_DELIVERY_JOBS)
                .withDescription("Deliver via Drone Job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    public Trigger buildJobTrigger(JobDetail jobDetail, LocalDateTime startAt) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "delivery-triggers")
                .withDescription("Deliver via Drone Trigger")
                .startAt(Date.from(startAt.atZone(ZoneId.systemDefault()).toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }

    public String scheduleJob(JobDetail jobDetail, Trigger trigger) {
        try {
            scheduler.scheduleJob(jobDetail, trigger);
            return jobDetail.getKey().getName();
        } catch (SchedulerException ex) {
            log.error("Error scheduling delivery", ex);
            throw new IllegalStateException("Error scheduling delivery. Please try later!");
        }
    }

    public void deleteJob(String jobKey) {
        try {
            scheduler.deleteJob(new JobKey(jobKey, JOB_GROUP_IDENTITY_DELIVERY_JOBS));
        } catch (SchedulerException ex) {
            log.error("Error deleting scheduled delivery job", ex);
            throw new IllegalStateException("Error deleting scheduled delivery job.");
        }
    }
}

