package org.synyx.urlaubsverwaltung.cron;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.AbsenceSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createVacationType;

@RunWith(MockitoJUnitRunner.class)
public class CronMailServiceTest {

    private CronMailService sut;

    @Mock
    private ApplicationService applicationService;
    @Mock
    private SettingsService settingsService;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private MailService mailService;

    @Before
    public void setUp() {
        sut = new CronMailService(applicationService, settingsService, sickNoteService, mailService);
    }

    @Test
    public void ensureSendEndOfSickPayNotification() {

        final Person person = new Person();
        person.setLoginName("Hulk");

        final SickNote sickNoteA = new SickNote();
        sickNoteA.setId(1);
        sickNoteA.setPerson(person);

        final SickNote sickNoteB = new SickNote();
        sickNoteB.setId(2);
        sickNoteB.setPerson(person);

        when(sickNoteService.getSickNotesReachingEndOfSickPay()).thenReturn(asList(sickNoteA, sickNoteB));

        prepareSettingsWithMaximumSickPayDays(5);

        Map<String, Object> modelA = new HashMap<>();
        modelA.put("maximumSickPayDays", 5);
        modelA.put("sickNote", sickNoteA);

        Map<String, Object> modelB = new HashMap<>();
        modelB.put("maximumSickPayDays", 5);
        modelB.put("sickNote", sickNoteB);

        sut.sendEndOfSickPayNotification();

        verify(mailService).sendMailTo(sickNoteA.getPerson(), "subject.sicknote.endOfSickPay", "sicknote_end_of_sick_pay", modelA);
        verify(mailService).sendMailTo(NOTIFICATION_OFFICE, "subject.sicknote.endOfSickPay", "sicknote_end_of_sick_pay", modelA);

        verify(mailService).sendMailTo(sickNoteB.getPerson(), "subject.sicknote.endOfSickPay", "sicknote_end_of_sick_pay", modelB);
        verify(mailService).sendMailTo(NOTIFICATION_OFFICE, "subject.sicknote.endOfSickPay", "sicknote_end_of_sick_pay", modelB);
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

        final VacationType vacationType = createVacationType(HOLIDAY);

        Application shortWaitingApplication = createApplication(createPerson("leo"), vacationType);
        shortWaitingApplication.setApplicationDate(LocalDate.now(UTC));

        Application longWaitingApplicationA = createApplication(createPerson("lea"), vacationType);
        longWaitingApplicationA.setApplicationDate(LocalDate.now(UTC).minusDays(3));

        Application longWaitingApplicationB = createApplication(createPerson("heinz"), vacationType);
        longWaitingApplicationB.setApplicationDate(LocalDate.now(UTC).minusDays(3));

        Application longWaitingApplicationAlreadyRemindedToday = createApplication(createPerson("heinz"), vacationType);
        longWaitingApplicationAlreadyRemindedToday.setApplicationDate(LocalDate.now(UTC).minusDays(3));
        LocalDate today = LocalDate.now(UTC);
        longWaitingApplicationAlreadyRemindedToday.setRemindDate(today);

        Application longWaitingApplicationAlreadyRemindedEarlier = createApplication(createPerson("heinz"), vacationType);
        longWaitingApplicationAlreadyRemindedEarlier.setApplicationDate(LocalDate.now(UTC).minusDays(5));
        LocalDate oldRemindDateEarlier = LocalDate.now(UTC).minusDays(3);
        longWaitingApplicationAlreadyRemindedEarlier.setRemindDate(oldRemindDateEarlier);

        List<Application> waitingApplications = asList(shortWaitingApplication,
            longWaitingApplicationA,
            longWaitingApplicationB,
            longWaitingApplicationAlreadyRemindedToday,
            longWaitingApplicationAlreadyRemindedEarlier);

        when(applicationService.getApplicationsForACertainState(WAITING)).thenReturn(waitingApplications);

        sut.sendWaitingApplicationsReminderNotification();

        // verify(mailService).sendRemindForWaitingApplicationsReminderNotification(asList(longWaitingApplicationA, longWaitingApplicationB, longWaitingApplicationAlreadyRemindedEarlier));

        assertTrue(longWaitingApplicationA.getRemindDate().isAfter(longWaitingApplicationA.getApplicationDate()));
        assertTrue(longWaitingApplicationB.getRemindDate().isAfter(longWaitingApplicationB.getApplicationDate()));
        assertTrue(longWaitingApplicationAlreadyRemindedEarlier.getRemindDate().isAfter(oldRemindDateEarlier));
        assertTrue(longWaitingApplicationAlreadyRemindedToday.getRemindDate().isEqual(today));
    }

    private void prepareSettingsWithRemindForWaitingApplications(Boolean isActive) {
        Settings settings = new Settings();
        AbsenceSettings absenceSettings = new AbsenceSettings();
        absenceSettings.setRemindForWaitingApplications(isActive);
        settings.setAbsenceSettings(absenceSettings);
        when(settingsService.getSettings()).thenReturn(settings);
    }

    private void prepareSettingsWithMaximumSickPayDays(Integer sickPayDays) {
        Settings settings = new Settings();
        AbsenceSettings absenceSettings = new AbsenceSettings();
        absenceSettings.setMaximumSickPayDays(sickPayDays);
        settings.setAbsenceSettings(absenceSettings);
        when(settingsService.getSettings()).thenReturn(settings);
    }
}
