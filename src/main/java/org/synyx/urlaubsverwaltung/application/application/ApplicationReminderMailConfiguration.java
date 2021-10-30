package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.synyx.urlaubsverwaltung.application.ApplicationProperties;

@Configuration
class ApplicationReminderMailConfiguration implements SchedulingConfigurer {

    private final ApplicationProperties applicationProperties;
    private final ApplicationReminderMailService applicationReminderMailService;

    @Autowired
    ApplicationReminderMailConfiguration(ApplicationProperties applicationProperties, ApplicationReminderMailService applicationReminderMailService) {
        this.applicationProperties = applicationProperties;
        this.applicationReminderMailService = applicationReminderMailService;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addCronTask(applicationReminderMailService::sendWaitingApplicationsReminderNotification, applicationProperties.getReminderNotification().getCron());
        scheduledTaskRegistrar.addCronTask(applicationReminderMailService::sendUpcomingApplicationsReminderNotification, applicationProperties.getUpcomingNotification().getCron());
        scheduledTaskRegistrar.addCronTask(applicationReminderMailService::sendUpcomingHolidayReplacementReminderNotification, applicationProperties.getUpcomingHolidayReplacementNotification().getCron());
    }
}
