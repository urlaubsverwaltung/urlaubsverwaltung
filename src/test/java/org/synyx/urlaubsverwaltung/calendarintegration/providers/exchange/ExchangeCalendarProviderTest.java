package org.synyx.urlaubsverwaltung.calendarintegration.providers.exchange;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.folder.CalendarFolder;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import microsoft.exchange.webservices.data.property.complex.Attendee;
import microsoft.exchange.webservices.data.property.complex.AttendeeCollection;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.property.complex.time.TimeZoneDefinition;
import microsoft.exchange.webservices.data.search.FindFoldersResults;
import microsoft.exchange.webservices.data.search.FolderView;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarMailService;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.settings.ExchangeCalendarSettings;

import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExchangeCalendarProviderTest {

    @Test
    public void checkCalendarSyncSettingsNoExceptionForEmptyEmail() {
        final CalendarMailService calendarMailService = mock(CalendarMailService.class);

        ExchangeCalendarProvider cut = new ExchangeCalendarProvider(calendarMailService);

        CalendarSettings calendarSettings = mock(CalendarSettings.class);
        ExchangeCalendarSettings exchangeCalSettings = mock(ExchangeCalendarSettings.class);

        when(calendarSettings.getExchangeCalendarSettings()).thenReturn(exchangeCalSettings);
        when(exchangeCalSettings.getEmail()).thenReturn("");

        cut.checkCalendarSyncSettings(calendarSettings); // no Exception is test enough
    }

    private CalendarSettings getMockedCalendarSettings() {
        CalendarSettings calendarSettings = mock(CalendarSettings.class);
        ExchangeCalendarSettings exchangeCalSettings = mock(ExchangeCalendarSettings.class);
        when(exchangeCalSettings.getTimeZoneId()).thenReturn("Europe/Berlin");

        when(calendarSettings.getExchangeCalendarSettings()).thenReturn(exchangeCalSettings);
        when(exchangeCalSettings.getEmail()).thenReturn("test@test.de");
        when(exchangeCalSettings.getPassword()).thenReturn("secret");
        when(exchangeCalSettings.getCalendar()).thenReturn("");

        return calendarSettings;
    }

    private ExchangeService getMockedExchangeService() throws Exception {

        ExchangeService exchangeService = mock(ExchangeService.class);

        FindFoldersResults calendarRoot = new FindFoldersResults();

        CalendarFolder folder = mock(CalendarFolder.class);

        when(folder.getDisplayName()).thenReturn("");
        when(folder.getId()).thenReturn(new FolderId("folder-id"));

        calendarRoot.getFolders().add(folder);

        when(exchangeService.findFolders(eq(WellKnownFolderName.Calendar), any(FolderView.class))).thenReturn(calendarRoot);
        when(exchangeService.getRequestedServerVersion()).thenReturn(ExchangeVersion.Exchange2010_SP2);

        return exchangeService;
    }

    private Appointment getMockedAppointment() throws Exception {
        Appointment appointment = mock(Appointment.class);


        AttendeeCollection attendeeCollection = new AttendeeCollection();
        attendeeCollection.add(new Attendee("smtpAddress"));

        when(appointment.getRequiredAttendees()).thenReturn(attendeeCollection);
        when(appointment.getId()).thenReturn(new ItemId("item-id"));

        return appointment;
    }


    @Test
    public void add() throws Exception {
        CalendarMailService calendarMailService = mock(CalendarMailService.class);
        ExchangeFactory exchangeFactory = mock(ExchangeFactory.class);

        Appointment appointment = getMockedAppointment();
        when(exchangeFactory.getNewAppointment(any(ExchangeService.class))).thenReturn(appointment);

        ExchangeService exchangeService = getMockedExchangeService();
        ExchangeCalendarProvider cut = new ExchangeCalendarProvider(exchangeService, exchangeFactory, calendarMailService);

        Absence absence = mock(Absence.class);
        when(absence.getPerson()).thenReturn(new Person("username", "lastName", "firstName", "abc@de.f"));
        when(absence.getStartDate()).thenReturn(ZonedDateTime.now(UTC));
        when(absence.getEndDate()).thenReturn(ZonedDateTime.now(UTC));

        CalendarSettings calendarSettings = getMockedCalendarSettings();

        assertEquals("item-id", cut.add(absence, calendarSettings).orElse("bad-id"));

        verify(appointment, times(1)).setStartTimeZone(any(TimeZoneDefinition.class));
        verify(appointment, times(1)).setEndTimeZone(any(TimeZoneDefinition.class));
    }
}
