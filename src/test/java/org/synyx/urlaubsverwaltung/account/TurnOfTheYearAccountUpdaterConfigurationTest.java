package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.synyx.urlaubsverwaltung.config.ScheduleLocking;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsSecondArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TurnOfTheYearAccountUpdaterConfigurationTest {

    @Mock
    private ScheduleLocking scheduleLocking;
    @Mock
    private TaskScheduler taskScheduler;

    @Test
    void updatesAccountsWithGivenCronJobInterval() {

        when(scheduleLocking.withLock(eq("UpdateAccountsForNextPeriod"), any(Runnable.class))).thenAnswer(returnsSecondArg());

        final AccountProperties properties = new AccountProperties();
        final TurnOfTheYearAccountUpdaterService service = mock(TurnOfTheYearAccountUpdaterService.class);
        final TurnOfTheYearAccountUpdaterConfiguration sut = new TurnOfTheYearAccountUpdaterConfiguration(properties, service, scheduleLocking, taskScheduler);

        final ScheduledTaskRegistrar taskRegistrar = new ScheduledTaskRegistrar();
        sut.configureTasks(taskRegistrar);

        final List<CronTask> cronTaskList = taskRegistrar.getCronTaskList();
        assertThat(cronTaskList).hasSize(1);

        final CronTask cronTask = cronTaskList.get(0);
        assertThat(cronTask.getExpression()).isEqualTo("0 0 5 1 1 *");

        verifyNoInteractions(service);

        cronTask.getRunnable().run();
        verify(service).updateAccountsForNextPeriod();
    }
}
