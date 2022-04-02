package org.synyx.urlaubsverwaltung.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class VacationDaysReminderConfiguration implements SchedulingConfigurer {

    private final AccountProperties accountProperties;
    private final VacationDaysReminderService vacationDaysReminderService;

    @Autowired
    public VacationDaysReminderConfiguration(AccountProperties accountProperties, VacationDaysReminderService vacationDaysReminderService) {
        this.accountProperties = accountProperties;
        this.vacationDaysReminderService = vacationDaysReminderService;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addCronTask(vacationDaysReminderService::remindForVacationDaysLeft, accountProperties.getVacationDaysReminder().getVacationDaysLeftCron());
        scheduledTaskRegistrar.addCronTask(vacationDaysReminderService::remindForRemainingVacationDays, accountProperties.getVacationDaysReminder().getTurnOfTheYearCron());
        scheduledTaskRegistrar.addCronTask(vacationDaysReminderService::remindForExpiredRemainingVacationDays, accountProperties.getVacationDaysReminder().getExpiredRemainingVacationDaysCron());
    }
}
