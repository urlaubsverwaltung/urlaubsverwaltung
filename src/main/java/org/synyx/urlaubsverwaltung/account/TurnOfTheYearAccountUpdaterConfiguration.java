package org.synyx.urlaubsverwaltung.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class TurnOfTheYearAccountUpdaterConfiguration implements SchedulingConfigurer {

    private final AccountProperties accountProperties;
    private final TurnOfTheYearAccountUpdaterService turnOfTheYearAccountUpdaterService;

    @Autowired
    public TurnOfTheYearAccountUpdaterConfiguration(AccountProperties accountProperties, TurnOfTheYearAccountUpdaterService turnOfTheYearAccountUpdaterService) {
        this.accountProperties = accountProperties;
        this.turnOfTheYearAccountUpdaterService = turnOfTheYearAccountUpdaterService;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addCronTask(turnOfTheYearAccountUpdaterService::updateAccountsForNextPeriod, accountProperties.getUpdate().getCron());
    }
}
