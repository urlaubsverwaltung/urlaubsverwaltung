package org.synyx.urlaubsverwaltung.application.service;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.synyx.urlaubsverwaltung.application.ApplicationProperties;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class ApplicationCronMailConfigurationTest {

    @Test
    public void sendsWaitingApplicationReminderWithGivenCronJobInterval() {

        final ApplicationProperties properties = new ApplicationProperties();
        final ApplicationCronMailService service = mock(ApplicationCronMailService.class);
        final ApplicationCronMailConfiguration sut = new ApplicationCronMailConfiguration(properties, service);

        final ScheduledTaskRegistrar taskRegistrar = new ScheduledTaskRegistrar();
        sut.configureTasks(taskRegistrar);

        final List<CronTask> cronTaskList = taskRegistrar.getCronTaskList();
        Assertions.assertThat(cronTaskList).hasSize(1);

        final CronTask cronTask = cronTaskList.get(0);
        Assertions.assertThat(cronTask.getExpression()).isEqualTo("0 0 7 * * *");

        verifyZeroInteractions(service);

        cronTask.getRunnable().run();
        verify(service).sendWaitingApplicationsReminderNotification();
    }
}
