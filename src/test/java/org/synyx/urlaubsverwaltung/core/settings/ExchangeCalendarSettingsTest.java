package org.synyx.urlaubsverwaltung.core.settings;

import org.junit.Assert;
import org.junit.Test;


/**
 * Unit test for {@link ExchangeCalendarSettings}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ExchangeCalendarSettingsTest {

    @Test
    public void ensureHasSomeDefaultValues() {

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
