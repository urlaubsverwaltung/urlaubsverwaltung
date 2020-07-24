package org.synyx.urlaubsverwaltung.settings;

import org.junit.Assert;
import org.junit.jupiter.api.Test;


/**
 * Unit test for {@link ExchangeCalendarSettings}.
 */
class ExchangeCalendarSettingsTest {

    @Test
    void ensureHasSomeDefaultValues() {

        ExchangeCalendarSettings calendarSettings = new ExchangeCalendarSettings();

        // No default values
        Assert.assertNull("Should be null", calendarSettings.getEmail());
        Assert.assertNull("Should be null", calendarSettings.getPassword());

        // Default values
        Assert.assertNotNull("Should be set", calendarSettings.getCalendar());
        Assert.assertEquals("Wrong calendar name", "", calendarSettings.getCalendar());
        Assert.assertFalse("Should not send invitations", calendarSettings.isSendInvitationActive());
    }
}
