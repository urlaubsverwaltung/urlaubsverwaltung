package org.synyx.urlaubsverwaltung.application.service;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.settings.AbsenceSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createVacationType;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationCronMailServiceTest {

    private ApplicationCronMailService sut;

    @Mock
    private ApplicationService applicationService;
    @Mock
    private SettingsService settingsService;
    @Mock
    private ApplicationMailService applicationMailService;

    @Before
    public void setUp() {
        sut = new ApplicationCronMailService(applicationService, settingsService, applicationMailService, Clock.systemUTC());
    }

    @Test
    public void ensureSendWaitingApplicationsReminderNotification() {

        boolean isActive = true;
        prepareSettingsWithRemindForWaitingApplications(isActive);

        final VacationType vacationType = createVacationType(HOLIDAY);

        final Application shortWaitingApplication = createApplication(createPerson("leo"), vacationType);
        shortWaitingApplication.setApplicationDate(Instant.now());

        final Application longWaitingApplicationA = createApplication(createPerson("lea"), vacationType);
        longWaitingApplicationA.setApplicationDate(Instant.now().minus(3, DAYS));

        final Application longWaitingApplicationB = createApplication(createPerson("heinz"), vacationType);
        longWaitingApplicationB.setApplicationDate(Instant.now().minus(3, DAYS));

        final Application longWaitingApplicationAlreadyRemindedToday = createApplication(createPerson("heinz"), vacationType);
        longWaitingApplicationAlreadyRemindedToday.setApplicationDate(Instant.now().minus(3, DAYS));
        Instant today = Instant.now();
        longWaitingApplicationAlreadyRemindedToday.setRemindDate(today);

        final Application longWaitingApplicationAlreadyRemindedEarlier = createApplication(createPerson("heinz"), vacationType);
        longWaitingApplicationAlreadyRemindedEarlier.setApplicationDate(Instant.now().minus(5, DAYS));
        Instant oldRemindDateEarlier = Instant.now().minus(3, DAYS);
        longWaitingApplicationAlreadyRemindedEarlier.setRemindDate(oldRemindDateEarlier);

        final List<Application> waitingApplications = asList(shortWaitingApplication,
            longWaitingApplicationA,
            longWaitingApplicationB,
            longWaitingApplicationAlreadyRemindedToday,
            longWaitingApplicationAlreadyRemindedEarlier);

        when(applicationService.getApplicationsForACertainState(WAITING)).thenReturn(waitingApplications);

        sut.sendWaitingApplicationsReminderNotification();

        verify(applicationMailService).sendRemindForWaitingApplicationsReminderNotification(asList(longWaitingApplicationA, longWaitingApplicationB, longWaitingApplicationAlreadyRemindedEarlier));

        assertTrue(longWaitingApplicationA.getRemindDate().isAfter(longWaitingApplicationA.getApplicationDate()));
        assertTrue(longWaitingApplicationB.getRemindDate().isAfter(longWaitingApplicationB.getApplicationDate()));
        assertTrue(longWaitingApplicationAlreadyRemindedEarlier.getRemindDate().isAfter(oldRemindDateEarlier));
        assertEquals(longWaitingApplicationAlreadyRemindedToday.getRemindDate(), today);
    }


    private void prepareSettingsWithRemindForWaitingApplications(Boolean isActive) {
        Settings settings = new Settings();
        AbsenceSettings absenceSettings = new AbsenceSettings();
        absenceSettings.setRemindForWaitingApplications(isActive);
        settings.setAbsenceSettings(absenceSettings);
        when(settingsService.getSettings()).thenReturn(settings);
    }
}
