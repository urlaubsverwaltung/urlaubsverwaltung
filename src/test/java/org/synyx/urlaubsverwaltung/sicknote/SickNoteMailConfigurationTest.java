package org.synyx.urlaubsverwaltung.sicknote;

import org.junit.Test;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class SickNoteMailConfigurationTest {

    @Test
    public void sendsEMailWithGivenCronJobInterval() {

        final SickNoteProperties properties = new SickNoteProperties();
        final SickNoteMailService sickNoteMailService = mock(SickNoteMailService.class);
        final SickNoteMailConfiguration sut = new SickNoteMailConfiguration(properties, sickNoteMailService);

        final ScheduledTaskRegistrar taskRegistrar = new ScheduledTaskRegistrar();
        sut.configureTasks(taskRegistrar);

        final List<CronTask> cronTaskList = taskRegistrar.getCronTaskList();
        assertThat(cronTaskList).hasSize(1);

        final CronTask cronTask = cronTaskList.get(0);
        assertThat(cronTask.getExpression()).isEqualTo("0 0 6 * * *");

        verifyZeroInteractions(sickNoteMailService);

        cronTask.getRunnable().run();
        verify(sickNoteMailService).sendEndOfSickPayNotification();
    }
}
