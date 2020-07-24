package org.synyx.urlaubsverwaltung.settings;

import org.junit.Assert;
import org.junit.jupiter.api.Test;


/**
 * Unit test for {@link CalendarSettings}.
 */
class CalendarSettingsTest {

    @Test
    void ensureHasDefaultValues() {

        CalendarSettings calendarSettings = new CalendarSettings();

        Assert.assertNotNull("Should not be null", calendarSettings.getExchangeCalendarSettings());
        Assert.assertNotNull("Should not be null", calendarSettings.getWorkDayBeginHour());
        Assert.assertNotNull("Should not be null", calendarSettings.getWorkDayEndHour());

        Assert.assertEquals("Wrong begin of work day", (Integer) 8, calendarSettings.getWorkDayBeginHour());
        Assert.assertEquals("Wrong end of work day", (Integer) 16, calendarSettings.getWorkDayEndHour());
    }
}
