package org.synyx.urlaubsverwaltung.core.sync.providers.exchange;

import org.junit.Test;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.ExchangeCalendarSettings;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class ExchangeCalendarProviderTest {

    @Test
    public void checkCalendarSyncSettingsNoExceptionForEmptyEmail() {
        MailService mailService = Mockito.mock(MailService.class);

        ExchangeCalendarProvider cut = new ExchangeCalendarProvider(mailService);

        CalendarSettings calendarSettings = Mockito.mock(CalendarSettings.class);
        ExchangeCalendarSettings exchangeCalSettings = Mockito.mock(ExchangeCalendarSettings.class);

        when(calendarSettings.getExchangeCalendarSettings()).thenReturn(exchangeCalSettings);
        when(exchangeCalSettings.getEmail()).thenReturn("");

        cut.checkCalendarSyncSettings(calendarSettings); // no Exception is test enough
    }
}