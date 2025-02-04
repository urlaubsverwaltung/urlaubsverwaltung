package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.synyx.urlaubsverwaltung.application.ApplicationProperties;
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
class ApplicationReminderMailConfigurationTest {

    @Mock
    private ScheduleLocking scheduleLocking;
    @Mock
    private TaskScheduler taskScheduler;

    @Test
    void sendsWaitingApplicationReminderWithGivenCronJobInterval() {

        when(scheduleLocking.withLock(eq("WaitingApplicationsReminderNotification"), any(Runnable.class))).thenAnswer(returnsSecondArg());
        when(scheduleLocking.withLock(eq("UpcomingApplicationsReminderNotification"), any(Runnable.class))).thenAnswer(returnsSecondArg());
        when(scheduleLocking.withLock(eq("UpcomingHolidayReplacementReminderNotification"), any(Runnable.class))).thenAnswer(returnsSecondArg());

        final ApplicationProperties properties = new ApplicationProperties();
        final ApplicationReminderMailService service = mock(ApplicationReminderMailService.class);
        final ApplicationReminderMailConfiguration sut = new ApplicationReminderMailConfiguration(properties, service, scheduleLocking, taskScheduler);

        final ScheduledTaskRegistrar taskRegistrar = new ScheduledTaskRegistrar();
        sut.configureTasks(taskRegistrar);

        final List<CronTask> cronTaskList = taskRegistrar.getCronTaskList();
        assertThat(cronTaskList).hasSize(3);

        verifyNoInteractions(service);

        // Waiting Application Reminder
        final CronTask cronTask = cronTaskList.get(0);
        assertThat(cronTask.getExpression()).isEqualTo("0 0 7 * * *");

        cronTask.getRunnable().run();
        verify(service).sendWaitingApplicationsReminderNotification();

        // Upcoming Application Reminder
        final CronTask cronTaskStartsSoon = cronTaskList.get(1);
        assertThat(cronTaskStartsSoon.getExpression()).isEqualTo("0 0 7 * * *");

        cronTaskStartsSoon.getRunnable().run();
        verify(service).sendUpcomingApplicationsReminderNotification();

        // Upcoming replacement Reminder
        final CronTask cronTaskHolidayReplacementStartsSoon = cronTaskList.get(2);
        assertThat(cronTaskHolidayReplacementStartsSoon.getExpression()).isEqualTo("0 0 7 * * *");

        cronTaskHolidayReplacementStartsSoon.getRunnable().run();
        verify(service).sendUpcomingHolidayReplacementReminderNotification();
    }
}
