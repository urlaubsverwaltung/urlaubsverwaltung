package org.synyx.urlaubsverwaltung.core.sync.providers.google;

import org.joda.time.DateMidnight;
import org.junit.Test;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.period.Period;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.GoogleCalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.core.sync.absence.EventType;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class GoogleCalendarSyncProviderServiceTest {

    GoogleCalendarSyncProviderService cut;

    @Test
    public void init() {
        MailService mailService = Mockito.mock(MailService.class);

        SettingsService settingsService = Mockito.mock(SettingsService.class);
        Settings set = Mockito.mock(Settings.class);
        CalendarSettings cSet = Mockito.mock(CalendarSettings.class);
        GoogleCalendarSettings gcSet = Mockito.mock(GoogleCalendarSettings.class);
        Mockito.when(settingsService.getSettings()).thenReturn(set);
        Mockito.when(set.getCalendarSettings()).thenReturn(cSet);
        Mockito.when(cSet.getGoogleCalendarSettings()).thenReturn(gcSet);
        Mockito.when(gcSet.getCalendar()).thenReturn("calendarName");
        Mockito.when(gcSet.getCalendarId()).thenReturn("calenderId");
        Mockito.when(gcSet.getServiceAccount()).thenReturn("serviceAccount");
        Mockito.when(gcSet.getClientSecretFile()).thenReturn("clientSecretFile.json");

        cut = new GoogleCalendarSyncProviderService(mailService, settingsService);

        System.out.println("hello world");
    }

    @Test
    public void addAbsence() {
        MailService mailService = Mockito.mock(MailService.class);

        SettingsService settingsService = Mockito.mock(SettingsService.class);
        Settings set = Mockito.mock(Settings.class);
        CalendarSettings cSet = Mockito.mock(CalendarSettings.class);
        GoogleCalendarSettings gcSet = Mockito.mock(GoogleCalendarSettings.class);
        Mockito.when(settingsService.getSettings()).thenReturn(set);
        Mockito.when(set.getCalendarSettings()).thenReturn(cSet);
        Mockito.when(cSet.getGoogleCalendarSettings()).thenReturn(gcSet);
        Mockito.when(gcSet.getCalendar()).thenReturn("calendarName");
        Mockito.when(gcSet.getCalendarId()).thenReturn("lange@synyx.de");
        Mockito.when(gcSet.getServiceAccount()).thenReturn(null);
        Mockito.when(gcSet.getClientSecretFile()).thenReturn("client_secret.json");

        cut = new GoogleCalendarSyncProviderService(mailService, settingsService);
        Person person = new Person();
        Period period = new Period(new DateMidnight(0), new DateMidnight(1), DayLength.MORNING);
        EventType type = EventType.SICKNOTE;
        CalendarSettings cSet2 = Mockito.mock(CalendarSettings.class);
        AbsenceTimeConfiguration config = new AbsenceTimeConfiguration(cSet2);
        Absence absence = new Absence(person, period, type, config);
        Optional opt = cut.add(absence, cSet);

        assertTrue(opt.isPresent());
        System.out.println("hello world");
    }
}
