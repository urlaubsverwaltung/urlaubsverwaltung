package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.synyx.urlaubsverwaltung.application.ApplicationProperties;
import org.synyx.urlaubsverwaltung.config.ScheduleLocking;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.ConditionalOnSingleTenantMode;

@Configuration
@ConditionalOnSingleTenantMode
class ApplicationReminderMailConfiguration implements SchedulingConfigurer {

    private final ApplicationProperties applicationProperties;
    private final ApplicationReminderMailService applicationReminderMailService;
    private final ScheduleLocking scheduleLocking;
    private final TaskScheduler taskScheduler;

    @Autowired
    ApplicationReminderMailConfiguration(ApplicationProperties applicationProperties, ApplicationReminderMailService applicationReminderMailService, ScheduleLocking scheduleLocking, TaskScheduler taskScheduler) {
        this.applicationProperties = applicationProperties;
        this.applicationReminderMailService = applicationReminderMailService;
        this.scheduleLocking = scheduleLocking;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler);
        taskRegistrar.addCronTask(
            scheduleLocking.withLock("WaitingApplicationsReminderNotification", applicationReminderMailService::sendWaitingApplicationsReminderNotification),
            applicationProperties.getReminderNotification().getCron()
        );
        taskRegistrar.addCronTask(
            scheduleLocking.withLock("UpcomingApplicationsReminderNotification", applicationReminderMailService::sendUpcomingApplicationsReminderNotification),
            applicationProperties.getUpcomingNotification().getCron()
        );
        taskRegistrar.addCronTask(
            scheduleLocking.withLock("UpcomingHolidayReplacementReminderNotification", applicationReminderMailService::sendUpcomingHolidayReplacementReminderNotification),
            applicationProperties.getUpcomingHolidayReplacementNotification().getCron()
        );
    }
}
