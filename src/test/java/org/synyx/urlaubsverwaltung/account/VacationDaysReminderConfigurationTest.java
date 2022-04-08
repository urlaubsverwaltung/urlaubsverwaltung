package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VacationDaysReminderConfigurationTest {

    private VacationDaysReminderConfiguration sut;

    @Mock
    private VacationDaysReminderService vacationDaysReminderService;

    @BeforeEach
    void setUp() {
        sut = new VacationDaysReminderConfiguration(new AccountProperties(), vacationDaysReminderService);
    }

    @Test
    void ensureCronTasksForReminderServiceAreAdded() {

        final ScheduledTaskRegistrar taskRegistrar = new ScheduledTaskRegistrar();
        sut.configureTasks(taskRegistrar);

        final List<CronTask> cronTaskList = taskRegistrar.getCronTaskList();
        assertThat(cronTaskList).hasSize(2);

        final CronTask remindForCurrentlyLeftVacationDaysCronTask = cronTaskList.get(0);
        assertThat(remindForCurrentlyLeftVacationDaysCronTask.getExpression()).isEqualTo("0 0 6 1 10 *");
        remindForCurrentlyLeftVacationDaysCronTask.getRunnable().run();
        verify(vacationDaysReminderService).remindForCurrentlyLeftVacationDays();

        final CronTask ForExpiredRemainingVacationDaysCronTask = cronTaskList.get(1);
        assertThat(ForExpiredRemainingVacationDaysCronTask.getExpression()).isEqualTo("0 0 6 1 4 *");
        ForExpiredRemainingVacationDaysCronTask.getRunnable().run();
        verify(vacationDaysReminderService).notifyForExpiredRemainingVacationDays();
    }
}
