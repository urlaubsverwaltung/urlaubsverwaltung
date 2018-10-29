package org.synyx.urlaubsverwaltung.core.sync.providers.exchange;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.folder.CalendarFolder;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import microsoft.exchange.webservices.data.property.complex.Attendee;
import microsoft.exchange.webservices.data.property.complex.AttendeeCollection;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.search.FindFoldersResults;
import org.junit.Test;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.ExchangeCalendarSettings;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.*;

public class ExchangeCalendarProviderTest {

    @Test
    public void checkCalendarSyncSettingsNoExceptionForEmptyEmail() {
        MailService mailService = mock(MailService.class);

        ExchangeCalendarProvider cut = new ExchangeCalendarProvider(mailService);

        CalendarSettings calendarSettings = mock(CalendarSettings.class);
        ExchangeCalendarSettings exchangeCalSettings = mock(ExchangeCalendarSettings.class);

        when(calendarSettings.getExchangeCalendarSettings()).thenReturn(exchangeCalSettings);
        when(exchangeCalSettings.getEmail()).thenReturn("");

        cut.checkCalendarSyncSettings(calendarSettings); // no Exception is test enough
    }

    private CalendarSettings getMockedCalendarSettings() {
        CalendarSettings calendarSettings = Mockito.mock(CalendarSettings.class);
        ExchangeCalendarSettings exchangeCalSettings = Mockito.mock(ExchangeCalendarSettings.class);
        when(exchangeCalSettings.getTimeZoneId()).thenReturn("Europe/Berlin");

        when(calendarSettings.getExchangeCalendarSettings()).thenReturn(exchangeCalSettings);
        when(exchangeCalSettings.getEmail()).thenReturn("test@test.de");
        when(exchangeCalSettings.getPassword()).thenReturn("secret");
        when(exchangeCalSettings.getCalendar()).thenReturn("");

        return calendarSettings;
    }

    private ExchangeService getMockedExchangeService() throws Exception {

        ExchangeService exchangeService = Mockito.mock(ExchangeService.class);

        FindFoldersResults calendarRoot = new FindFoldersResults();

        CalendarFolder folder = Mockito.mock(CalendarFolder.class);

        when(folder.getDisplayName()).thenReturn("");
        when(folder.getId()).thenReturn(new FolderId("folder-id"));

        calendarRoot.getFolders().add(folder);

        when(exchangeService.findFolders(eq(WellKnownFolderName.Calendar), anyObject())).thenReturn(calendarRoot);
        when(exchangeService.getRequestedServerVersion()).thenReturn(ExchangeVersion.Exchange2010_SP2);

        return exchangeService;
    }

    private Appointment getMockedAppointment() throws Exception {
        Appointment appointment = Mockito.mock(Appointment.class);


        AttendeeCollection attendeeCollection = new AttendeeCollection();
        attendeeCollection.add(new Attendee("smtpAddress"));

        when(appointment.getRequiredAttendees()).thenReturn(attendeeCollection);
        when(appointment.getId()).thenReturn(new ItemId("item-id"));

        return appointment;
    }


    @Test
    public void add() throws Exception {
        MailService mailService = Mockito.mock(MailService.class);
        ExchangeFactory exchangeFactory = Mockito.mock(ExchangeFactory.class);

        Appointment appointment = getMockedAppointment();
        when(exchangeFactory.getNewAppointment(anyObject())).thenReturn(appointment);

        ExchangeService exchangeService = getMockedExchangeService();
        ExchangeCalendarProvider cut = new ExchangeCalendarProvider(mailService, exchangeService, exchangeFactory);

        Absence absence = Mockito.mock(Absence.class);
        when(absence.getPerson()).thenReturn(new Person("login", "lastName", "firstName", "abc@de.f"));

        CalendarSettings calendarSettings = getMockedCalendarSettings();

        assertEquals(cut.add(absence, calendarSettings).get(), "item-id");

        verify(appointment, times(1)).setStartTimeZone(anyObject());
        verify(appointment, times(1)).setEndTimeZone(anyObject());
    }
}
