package org.synyx.urlaubsverwaltung.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.synyx.urlaubsverwaltung.config.ScheduleLocking;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.ConditionalOnSingleTenantMode;

@Configuration
@ConditionalOnSingleTenantMode
class VacationDaysReminderConfiguration implements SchedulingConfigurer {

    private final AccountProperties accountProperties;
    private final VacationDaysReminderService vacationDaysReminderService;
    private final ScheduleLocking scheduleLocking;
    private final TaskScheduler taskScheduler;

    @Autowired
    VacationDaysReminderConfiguration(AccountProperties accountProperties, VacationDaysReminderService vacationDaysReminderService, ScheduleLocking scheduleLocking, TaskScheduler taskScheduler) {
        this.accountProperties = accountProperties;
        this.vacationDaysReminderService = vacationDaysReminderService;
        this.scheduleLocking = scheduleLocking;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler);
        taskRegistrar.addCronTask(
            scheduleLocking.withLock("RemindForCurrentlyLeftVacationDays", vacationDaysReminderService::remindForCurrentlyLeftVacationDays),
            accountProperties.getVacationDaysReminder().getVacationDaysLeftCron()
        );
        taskRegistrar.addCronTask(
            scheduleLocking.withLock("NotifyForExpiredRemainingVacationDays", vacationDaysReminderService::notifyForExpiredRemainingVacationDays),
            accountProperties.getVacationDaysReminder().getExpiredRemainingVacationDaysCron()
        );
    }
}
