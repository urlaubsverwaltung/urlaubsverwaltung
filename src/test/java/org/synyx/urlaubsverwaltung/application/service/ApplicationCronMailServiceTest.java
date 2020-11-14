package org.synyx.urlaubsverwaltung.application.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.application.ApplicationSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;

@ExtendWith(MockitoExtension.class)
class ApplicationCronMailServiceTest {

    private ApplicationCronMailService sut;

    @Mock
    private ApplicationService applicationService;
    @Mock
    private SettingsService settingsService;
    @Mock
    private ApplicationMailService applicationMailService;

    @BeforeEach
    void setUp() {
        sut = new ApplicationCronMailService(applicationService, settingsService, applicationMailService, Clock.systemUTC());
    }

    @Test
    void ensureSendWaitingApplicationsReminderNotification() {

        boolean isActive = true;
        prepareSettingsWithRemindForWaitingApplications(isActive);

        final VacationType vacationType = createVacationType(HOLIDAY);

        final Application shortWaitingApplication = createApplication(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationType);
        shortWaitingApplication.setApplicationDate(LocalDate.now(UTC));

        final Application longWaitingApplicationA = createApplication(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationType);
        longWaitingApplicationA.setApplicationDate(LocalDate.now(UTC).minusDays(3));

        final Application longWaitingApplicationB = createApplication(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationType);
        longWaitingApplicationB.setApplicationDate(LocalDate.now(UTC).minusDays(3));

        final Application longWaitingApplicationAlreadyRemindedToday = createApplication(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationType);
        longWaitingApplicationAlreadyRemindedToday.setApplicationDate(LocalDate.now(UTC).minusDays(3));
        LocalDate today = LocalDate.now(UTC);
        longWaitingApplicationAlreadyRemindedToday.setRemindDate(today);

        final Application longWaitingApplicationAlreadyRemindedEarlier = createApplication(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationType);
        longWaitingApplicationAlreadyRemindedEarlier.setApplicationDate(LocalDate.now(UTC).minusDays(5));
        LocalDate oldRemindDateEarlier = LocalDate.now(UTC).minusDays(3);
        longWaitingApplicationAlreadyRemindedEarlier.setRemindDate(oldRemindDateEarlier);

        final List<Application> waitingApplications = asList(shortWaitingApplication,
            longWaitingApplicationA,
            longWaitingApplicationB,
            longWaitingApplicationAlreadyRemindedToday,
            longWaitingApplicationAlreadyRemindedEarlier);

        when(applicationService.getForStates(List.of(WAITING))).thenReturn(waitingApplications);

        sut.sendWaitingApplicationsReminderNotification();

        verify(applicationMailService).sendRemindForWaitingApplicationsReminderNotification(asList(longWaitingApplicationA, longWaitingApplicationB, longWaitingApplicationAlreadyRemindedEarlier));

        assertThat(longWaitingApplicationA.getRemindDate()).isAfter(longWaitingApplicationA.getApplicationDate());
        assertThat(longWaitingApplicationB.getRemindDate()).isAfter(longWaitingApplicationB.getApplicationDate());
        assertThat(longWaitingApplicationAlreadyRemindedEarlier.getRemindDate()).isAfter(oldRemindDateEarlier);
        assertThat(longWaitingApplicationAlreadyRemindedToday.getRemindDate()).isEqualTo(today);
    }

    private void prepareSettingsWithRemindForWaitingApplications(Boolean isActive) {
        Settings settings = new Settings();
        ApplicationSettings applicationSettings = new ApplicationSettings();
        applicationSettings.setRemindForWaitingApplications(isActive);
        settings.setApplicationSettings(applicationSettings);
        when(settingsService.getSettings()).thenReturn(settings);
    }
}
