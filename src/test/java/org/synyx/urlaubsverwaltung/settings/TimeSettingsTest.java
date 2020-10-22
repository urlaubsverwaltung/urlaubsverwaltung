package org.synyx.urlaubsverwaltung.settings;

import org.junit.Assert;
import org.junit.jupiter.api.Test;


/**
 * Unit test for {@link CalendarSettings}.
 */
class TimeSettingsTest {

    @Test
    void ensureHasDefaultValues() {

        TimeSettings timeSettings = new TimeSettings();

        Assert.assertNotNull("Should not be null", timeSettings.getWorkDayBeginHour());
        Assert.assertNotNull("Should not be null", timeSettings.getWorkDayEndHour());

        Assert.assertEquals("Wrong begin of work day", (Integer) 8, timeSettings.getWorkDayBeginHour());
        Assert.assertEquals("Wrong end of work day", (Integer) 16, timeSettings.getWorkDayEndHour());
    }
}
