package org.synyx.urlaubsverwaltung.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.synyx.urlaubsverwaltung.application.ApplicationProperties;

@Configuration
class ApplicationCronMailConfiguration implements SchedulingConfigurer {

    private final ApplicationProperties applicationProperties;
    private final ApplicationCronMailService applicationCronMailService;

    @Autowired
    ApplicationCronMailConfiguration(ApplicationProperties applicationProperties, ApplicationCronMailService applicationCronMailService) {
        this.applicationProperties = applicationProperties;
        this.applicationCronMailService = applicationCronMailService;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addCronTask(applicationCronMailService::sendWaitingApplicationsReminderNotification, applicationProperties.getReminderNotification().getCron());
    }
}
