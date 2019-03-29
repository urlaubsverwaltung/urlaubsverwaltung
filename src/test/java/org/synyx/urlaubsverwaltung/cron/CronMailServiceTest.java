package org.synyx.urlaubsverwaltung.cron;

import org.joda.time.DateMidnight;
import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.settings.AbsenceSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class CronMailServiceTest {

    private CronMailService sut;
    private ApplicationService applicationService;
    private SettingsService settingsService;
    private SickNoteService sickNoteService;
    private MailService mailService;

    @Before
    public void setUp() {

        applicationService = mock(ApplicationService.class);
        settingsService = mock(SettingsService.class);
        sickNoteService = mock(SickNoteService.class);
        mailService = mock(MailService.class);

        sut = new CronMailService(applicationService, settingsService, sickNoteService, mailService);
    }

    @Test
    public void ensureSendEndOfSickPayNotification() {

        SickNote sickNoteA = new SickNote();
        SickNote sickNoteB = new SickNote();
        when(sickNoteService.getSickNotesReachingEndOfSickPay()).thenReturn(Arrays.asList(sickNoteA, sickNoteB));

        sut.sendEndOfSickPayNotification();

        verify(mailService).sendEndOfSickPayNotification(sickNoteA);
        verify(mailService).sendEndOfSickPayNotification(sickNoteB);
    }

    @Test
    public void ensureNoSendWhenDeactivated() {

        boolean isInactive = false;
        prepareSettingsWithRemindForWaitingApplications(isInactive);

        sut.sendEndOfSickPayNotification();
        verifyZeroInteractions(mailService);
    }

    @Test
    public void ensureSendWaitingApplicationsReminderNotification() {

        boolean isActive = true;
        prepareSettingsWithRemindForWaitingApplications(isActive);

        Application shortWaitingApplication = TestDataCreator.createApplication(TestDataCreator.createPerson("leo"), TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
        shortWaitingApplication.setApplicationDate(DateMidnight.now());

        Application longWaitingApplicationA = TestDataCreator.createApplication(TestDataCreator.createPerson("lea"), TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
        longWaitingApplicationA.setApplicationDate(DateMidnight.now().minusDays(3));

        Application longWaitingApplicationB = TestDataCreator.createApplication(TestDataCreator.createPerson("heinz"), TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
        longWaitingApplicationB.setApplicationDate(DateMidnight.now().minusDays(3));

        Application longWaitingApplicationAlreadyRemindedToday = TestDataCreator.createApplication(TestDataCreator.createPerson("heinz"), TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
        longWaitingApplicationAlreadyRemindedToday.setApplicationDate(DateMidnight.now().minusDays(3));
        DateMidnight today = DateMidnight.now();
        longWaitingApplicationAlreadyRemindedToday.setRemindDate(today);

        Application longWaitingApplicationAlreadyRemindedEalier = TestDataCreator.createApplication(TestDataCreator.createPerson("heinz"), TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
        longWaitingApplicationAlreadyRemindedEalier.setApplicationDate(DateMidnight.now().minusDays(5));
        DateMidnight oldRemindDateEarlier = DateMidnight.now().minusDays(3);
        longWaitingApplicationAlreadyRemindedEalier.setRemindDate(oldRemindDateEarlier);

        List<Application> waitingApplications = Arrays.asList(shortWaitingApplication,
                                                              longWaitingApplicationA,
                                                              longWaitingApplicationB,
                                                              longWaitingApplicationAlreadyRemindedToday,
                                                              longWaitingApplicationAlreadyRemindedEalier);

        when(applicationService.getApplicationsForACertainState(ApplicationStatus.WAITING)).thenReturn(waitingApplications);

        sut.sendWaitingApplicationsReminderNotification();

        verify(mailService).sendRemindForWaitingApplicationsReminderNotification(Arrays.asList(longWaitingApplicationA, longWaitingApplicationB, longWaitingApplicationAlreadyRemindedEalier));

        assertTrue(longWaitingApplicationA.getRemindDate().isAfter(longWaitingApplicationA.getApplicationDate()));
        assertTrue(longWaitingApplicationB.getRemindDate().isAfter(longWaitingApplicationB.getApplicationDate()));
        assertTrue(longWaitingApplicationAlreadyRemindedEalier.getRemindDate().isAfter(oldRemindDateEarlier));
        assertTrue(longWaitingApplicationAlreadyRemindedToday.getRemindDate().isEqual(today));
    }

    private void prepareSettingsWithRemindForWaitingApplications(Boolean isActive) {
        Settings settings = new Settings();
        AbsenceSettings absenceSettings = new AbsenceSettings();
        absenceSettings.setRemindForWaitingApplications(isActive);
        settings.setAbsenceSettings(absenceSettings);
        when(settingsService.getSettings()).thenReturn(settings);
    }

}
