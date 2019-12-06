package org.synyx.urlaubsverwaltung.account.service;

import org.junit.Test;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.synyx.urlaubsverwaltung.account.config.AccountProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class TurnOfTheYearAccountUpdaterConfigurationTest {

    @Test
    public void updatesAccountsWithGivenCronJobInterval() {

        final AccountProperties properties = new AccountProperties();
        final TurnOfTheYearAccountUpdaterService service = mock(TurnOfTheYearAccountUpdaterService.class);
        final TurnOfTheYearAccountUpdaterConfiguration sut = new TurnOfTheYearAccountUpdaterConfiguration(properties, service);

        final ScheduledTaskRegistrar taskRegistrar = new ScheduledTaskRegistrar();
        sut.configureTasks(taskRegistrar);

        final List<CronTask> cronTaskList = taskRegistrar.getCronTaskList();
        assertThat(cronTaskList).hasSize(1);

        final CronTask cronTask = cronTaskList.get(0);
        assertThat(cronTask.getExpression()).isEqualTo("0 0 5 1 1 *");

        verifyZeroInteractions(service);

        cronTask.getRunnable().run();
        verify(service).updateAccountsForNextPeriod();
    }
}
