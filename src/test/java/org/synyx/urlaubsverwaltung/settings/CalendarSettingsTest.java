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
    }
}
