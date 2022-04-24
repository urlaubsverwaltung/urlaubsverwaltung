package org.synyx.urlaubsverwaltung.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.synyx.urlaubsverwaltung.config.ScheduleLocking;

@Configuration
public class TurnOfTheYearAccountUpdaterConfiguration implements SchedulingConfigurer {

    private final AccountProperties accountProperties;
    private final TurnOfTheYearAccountUpdaterService turnOfTheYearAccountUpdaterService;
    private final ScheduleLocking scheduleLocking;

    @Autowired
    public TurnOfTheYearAccountUpdaterConfiguration(AccountProperties accountProperties, TurnOfTheYearAccountUpdaterService turnOfTheYearAccountUpdaterService, ScheduleLocking scheduleLocking) {
        this.accountProperties = accountProperties;
        this.turnOfTheYearAccountUpdaterService = turnOfTheYearAccountUpdaterService;
        this.scheduleLocking = scheduleLocking;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addCronTask(
            scheduleLocking.withLock("UpdateAccountsForNextPeriod", turnOfTheYearAccountUpdaterService::updateAccountsForNextPeriod),
            accountProperties.getUpdate().getCron()
        );
    }
}
