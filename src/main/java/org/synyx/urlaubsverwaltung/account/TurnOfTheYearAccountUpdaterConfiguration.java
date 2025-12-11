package org.synyx.urlaubsverwaltung.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.synyx.urlaubsverwaltung.config.ScheduleLocking;
import org.synyx.urlaubsverwaltung.extension.companyvacation.CompanyVacationEventRepublisher;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.ConditionalOnSingleTenantMode;

@Configuration
@ConditionalOnSingleTenantMode
class TurnOfTheYearAccountUpdaterConfiguration implements SchedulingConfigurer {

    private final AccountProperties accountProperties;
    private final TurnOfTheYearAccountUpdaterService turnOfTheYearAccountUpdaterService;
    private final ScheduleLocking scheduleLocking;
    private final TaskScheduler taskScheduler;
    private final CompanyVacationEventRepublisher companyVacationEventRepublisher;

    @Autowired
    TurnOfTheYearAccountUpdaterConfiguration(
        AccountProperties accountProperties, TurnOfTheYearAccountUpdaterService turnOfTheYearAccountUpdaterService,
        CompanyVacationEventRepublisher companyVacationEventRepublisher, ScheduleLocking scheduleLocking, TaskScheduler taskScheduler
    ) {
        this.accountProperties = accountProperties;
        this.turnOfTheYearAccountUpdaterService = turnOfTheYearAccountUpdaterService;
        this.scheduleLocking = scheduleLocking;
        this.taskScheduler = taskScheduler;
        this.companyVacationEventRepublisher = companyVacationEventRepublisher;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler);
        taskRegistrar.addCronTask(
            scheduleLocking.withLock("UpdateAccountsForNextPeriod", () -> {
                turnOfTheYearAccountUpdaterService.updateAccountsForNextPeriod();
                companyVacationEventRepublisher.republishEvents();
            }),
            accountProperties.getUpdate().getCron()
        );
    }
}
