package org.synyx.urlaubsverwaltung.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.synyx.urlaubsverwaltung.config.ScheduleLocking;

@Configuration
public class VacationDaysReminderConfiguration implements SchedulingConfigurer {

    private final AccountProperties accountProperties;
    private final VacationDaysReminderService vacationDaysReminderService;
    private final ScheduleLocking scheduleLocking;

    @Autowired
    VacationDaysReminderConfiguration(AccountProperties accountProperties, VacationDaysReminderService vacationDaysReminderService, ScheduleLocking scheduleLocking) {
        this.accountProperties = accountProperties;
        this.vacationDaysReminderService = vacationDaysReminderService;
        this.scheduleLocking = scheduleLocking;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addCronTask(scheduleLocking.withLock("RemindForCurrentlyLeftVacationDays", vacationDaysReminderService::remindForCurrentlyLeftVacationDays), accountProperties.getVacationDaysReminder().getVacationDaysLeftCron());
        scheduledTaskRegistrar.addCronTask(scheduleLocking.withLock("NotifyForExpiredRemainingVacationDays", vacationDaysReminderService::notifyForExpiredRemainingVacationDays), accountProperties.getVacationDaysReminder().getExpiredRemainingVacationDaysCron());
    }
}
