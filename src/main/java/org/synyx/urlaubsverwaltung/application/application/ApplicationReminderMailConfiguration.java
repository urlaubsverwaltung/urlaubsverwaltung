package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.synyx.urlaubsverwaltung.application.ApplicationProperties;
import org.synyx.urlaubsverwaltung.config.ScheduleLocking;

@Configuration
class ApplicationReminderMailConfiguration implements SchedulingConfigurer {

    private final ApplicationProperties applicationProperties;
    private final ApplicationReminderMailService applicationReminderMailService;
    private final ScheduleLocking scheduleLocking;

    @Autowired
    ApplicationReminderMailConfiguration(ApplicationProperties applicationProperties, ApplicationReminderMailService applicationReminderMailService, ScheduleLocking scheduleLocking) {
        this.applicationProperties = applicationProperties;
        this.applicationReminderMailService = applicationReminderMailService;
        this.scheduleLocking = scheduleLocking;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addCronTask(
            scheduleLocking.withLock("WaitingApplicationsReminderNotification", applicationReminderMailService::sendWaitingApplicationsReminderNotification),
            applicationProperties.getReminderNotification().getCron()
        );
        scheduledTaskRegistrar.addCronTask(
            scheduleLocking.withLock("UpcomingApplicationsReminderNotification", applicationReminderMailService::sendUpcomingApplicationsReminderNotification),
            applicationProperties.getUpcomingNotification().getCron()
        );
        scheduledTaskRegistrar.addCronTask(
            scheduleLocking.withLock("UpcomingHolidayReplacementReminderNotification", applicationReminderMailService::sendUpcomingHolidayReplacementReminderNotification),
            applicationProperties.getUpcomingHolidayReplacementNotification().getCron()
        );
    }
}
