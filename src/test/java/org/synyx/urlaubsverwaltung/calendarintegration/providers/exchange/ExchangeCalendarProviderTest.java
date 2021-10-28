package org.synyx.urlaubsverwaltung.calendarintegration.providers.exchange;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.service.folder.CalendarFolder;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import microsoft.exchange.webservices.data.property.complex.Attendee;
import microsoft.exchange.webservices.data.property.complex.AttendeeCollection;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.property.complex.time.TimeZoneDefinition;
import microsoft.exchange.webservices.data.search.FindFoldersResults;
import microsoft.exchange.webservices.data.search.FolderView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarMailService;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSettings;
import org.synyx.urlaubsverwaltung.calendarintegration.ExchangeCalendarSettings;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.Optional;

import static microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName.Calendar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeCalendarProviderTest {

    @Mock
    private CalendarMailService calendarMailService;
    @Mock
    private CalendarSettings calendarSettings;
    @Mock
    private ExchangeCalendarSettings exchangeCalSettings;
    @Mock
    private ExchangeService exchangeService;
    @Mock
    private ExchangeFactory exchangeFactory;

    @Test
    void isRealProviderConfigured() {
        final ExchangeCalendarProvider sut = new ExchangeCalendarProvider(calendarMailService);
        assertThat(sut.isRealProviderConfigured()).isTrue();
    }

    @Test
    void checkCalendarSyncSettingsNoExceptionForEmptyEmail() {

        final ExchangeCalendarProvider sut = new ExchangeCalendarProvider(calendarMailService);

        when(calendarSettings.getExchangeCalendarSettings()).thenReturn(exchangeCalSettings);
        when(exchangeCalSettings.getEmail()).thenReturn("");

        sut.checkCalendarSyncSettings(calendarSettings); // no Exception is test enough
    }

    @Test
    void add() throws Exception {
        final ExchangeService exchangeService = getExchangeService();

        final Appointment appointment = createAppointment();
        when(exchangeFactory.getNewAppointment(exchangeService)).thenReturn(appointment);

        final ExchangeCalendarProvider sut = new ExchangeCalendarProvider(exchangeService, exchangeFactory, calendarMailService);

        final Person person = new Person("username", "lastName", "firstName", "abc@de.f");

        final LocalDate start = LocalDate.of(2021, 1, 11);
        final LocalDate end = LocalDate.of(2021, 1, 12);
        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setTimeZoneId("Etc/UTC");
        final Absence absence = new Absence(person, new Period(start, end, DayLength.FULL), new AbsenceTimeConfiguration(timeSettings));

        final Optional<String> uniqueId = sut.add(absence, getCalendarSettings());
        assertThat(uniqueId).hasValue("item-id");

        verify(appointment).setStartTimeZone(any(TimeZoneDefinition.class));
        verify(appointment).setEndTimeZone(any(TimeZoneDefinition.class));
    }

    private CalendarSettings getCalendarSettings() {
        when(exchangeCalSettings.getTimeZoneId()).thenReturn("Europe/Berlin");

        when(calendarSettings.getExchangeCalendarSettings()).thenReturn(exchangeCalSettings);
        when(exchangeCalSettings.getEmail()).thenReturn("test@example.org");
        when(exchangeCalSettings.getPassword()).thenReturn("secret");
        when(exchangeCalSettings.getCalendar()).thenReturn("CalendarName");

        return calendarSettings;
    }

    private ExchangeService getExchangeService() throws Exception {

        final CalendarFolder folder = mock(CalendarFolder.class);
        when(folder.getDisplayName()).thenReturn("CalendarName");
        when(folder.getId()).thenReturn(new FolderId("folder-id"));

        final FindFoldersResults calendarRoot = new FindFoldersResults();
        calendarRoot.getFolders().add(folder);

        when(exchangeService.findFolders(eq(Calendar), any(FolderView.class))).thenReturn(calendarRoot);

        return exchangeService;
    }

    private Appointment createAppointment() throws Exception {

        final AttendeeCollection attendeeCollection = new AttendeeCollection();
        attendeeCollection.add(new Attendee("smtpAddress"));

        final Appointment appointment = mock(Appointment.class);
        when(appointment.getRequiredAttendees()).thenReturn(attendeeCollection);
        when(appointment.getId()).thenReturn(new ItemId("item-id"));

        return appointment;
    }
}
