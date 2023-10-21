package org.synyx.urlaubsverwaltung.application.application;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;

@ExtendWith(MockitoExtension.class)
class ApplicationReminderMailServiceTest {

    private ApplicationReminderMailService sut;

    @Mock
    private ApplicationService applicationService;
    @Mock
    private SettingsService settingsService;
    @Mock
    private ApplicationMailService applicationMailService;

    private final Clock clock = Clock.fixed(Instant.parse("2020-04-04T10:15:30.00Z"), UTC);

    @BeforeEach
    void setUp() {
        sut = new ApplicationReminderMailService(applicationService, settingsService, applicationMailService, clock);
    }

    @Test
    void ensureSendWaitingApplicationsReminderNotification() {

        boolean isActive = true;
        prepareSettingsWithRemindForWaitingApplications(isActive);

        final VacationType<?> vacationType = TestDataCreator.createVacationType(1L, HOLIDAY, new StaticMessageSource());

        final Application shortWaitingApplication = createApplication(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationType);
        shortWaitingApplication.setApplicationDate(LocalDate.now(clock));

        final Application longWaitingApplicationA = createApplication(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationType);
        longWaitingApplicationA.setApplicationDate(LocalDate.now(clock).minusDays(3));

        final Application longWaitingApplicationB = createApplication(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationType);
        longWaitingApplicationB.setApplicationDate(LocalDate.now(clock).minusDays(3));

        final Application longWaitingApplicationAlreadyRemindedToday = createApplication(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationType);
        longWaitingApplicationAlreadyRemindedToday.setApplicationDate(LocalDate.now(clock).minusDays(3));
        LocalDate today = LocalDate.now(clock);
        longWaitingApplicationAlreadyRemindedToday.setRemindDate(today);

        final Application longWaitingApplicationAlreadyRemindedEarlier = createApplication(new Person("muster", "Muster", "Marlene", "muster@example.org"), vacationType);
        longWaitingApplicationAlreadyRemindedEarlier.setApplicationDate(LocalDate.now(clock).minusDays(5));
        LocalDate oldRemindDateEarlier = LocalDate.now(clock).minusDays(3);
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

    @Test
    void sendUpcomingApplicationsReminderNotification() {

        final ApplicationSettings applicationSettings = prepareSettingsWithRemindForUpcomingApplications(true);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final VacationType<?> vacationType = TestDataCreator.createVacationType(1L, HOLIDAY, new StaticMessageSource());
        final LocalDate now = LocalDate.now(clock);
        final LocalDate to = now.plusDays(applicationSettings.getDaysBeforeRemindForUpcomingApplications());

        final Application tomorrowApplication = createApplication(person, vacationType);
        tomorrowApplication.setApplicationDate(to);

        when(applicationService.getApplicationsWhereApplicantShouldBeNotifiedAboutUpcomingApplication(now, to, List.of(ALLOWED, ALLOWED_CANCELLATION_REQUESTED, TEMPORARY_ALLOWED))).thenReturn(List.of(tomorrowApplication));

        sut.sendUpcomingApplicationsReminderNotification();
        verify(applicationMailService).sendRemindForUpcomingApplicationsReminderNotification(List.of(tomorrowApplication));

        final ArgumentCaptor<Application> applicationArgumentCaptor = ArgumentCaptor.forClass(Application.class);
        verify(applicationService).save(applicationArgumentCaptor.capture());
        assertThat(applicationArgumentCaptor.getAllValues().get(0).getUpcomingApplicationsReminderSend()).isEqualTo(now);
    }

    @Test
    void sendUpcomingApplicationsReminderNotificationDisabled() {

        prepareSettingsWithRemindForUpcomingApplications(false);

        sut.sendUpcomingApplicationsReminderNotification();
        verifyNoInteractions(applicationMailService);
    }

    @Test
    void sendUpcomingHolidayReplacementApplicationsReminderNotification() {

        final ApplicationSettings applicationSettings = prepareSettingsWithRemindForUpcomingHolidayReplacements(true);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate now = LocalDate.now(clock);
        final LocalDate to = now.plusDays(applicationSettings.getDaysBeforeRemindForUpcomingHolidayReplacement());

        final Application tomorrowApplication = createApplication(person, TestDataCreator.createVacationType(1L, HOLIDAY, new StaticMessageSource()));
        tomorrowApplication.setApplicationDate(to);

        when(applicationService.getApplicationsWhereHolidayReplacementShouldBeNotified(now, to, List.of(ALLOWED, ALLOWED_CANCELLATION_REQUESTED, TEMPORARY_ALLOWED))).thenReturn(List.of(tomorrowApplication));

        sut.sendUpcomingHolidayReplacementReminderNotification();
        verify(applicationMailService).sendRemindForUpcomingHolidayReplacement(List.of(tomorrowApplication));

        final ArgumentCaptor<Application> applicationArgumentCaptor = ArgumentCaptor.forClass(Application.class);
        verify(applicationService).save(applicationArgumentCaptor.capture());
        assertThat(applicationArgumentCaptor.getAllValues().get(0).getUpcomingHolidayReplacementNotificationSend()).isEqualTo(now);
    }

    @Test
    void sendUpcomingHolidayReplacementApplicationsReminderNotificationIsDisabled() {

        prepareSettingsWithRemindForUpcomingHolidayReplacements(false);

        sut.sendUpcomingHolidayReplacementReminderNotification();
        verifyNoInteractions(applicationMailService);
    }

    private ApplicationSettings prepareSettingsWithRemindForUpcomingHolidayReplacements(boolean activateUpcomingHolidayReplacements) {
        final Settings settings = new Settings();
        final ApplicationSettings applicationSettings = new ApplicationSettings();
        applicationSettings.setRemindForUpcomingHolidayReplacement(activateUpcomingHolidayReplacements);
        settings.setApplicationSettings(applicationSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        return applicationSettings;
    }

    private ApplicationSettings prepareSettingsWithRemindForUpcomingApplications(boolean activateUpcomingNotification) {
        final Settings settings = new Settings();
        final ApplicationSettings applicationSettings = new ApplicationSettings();
        applicationSettings.setRemindForUpcomingApplications(activateUpcomingNotification);
        settings.setApplicationSettings(applicationSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        return applicationSettings;
    }

    private void prepareSettingsWithRemindForWaitingApplications(boolean isActive) {
        final Settings settings = new Settings();
        final ApplicationSettings applicationSettings = new ApplicationSettings();
        applicationSettings.setRemindForWaitingApplications(isActive);
        settings.setApplicationSettings(applicationSettings);
        when(settingsService.getSettings()).thenReturn(settings);
    }
}
