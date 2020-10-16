package org.synyx.urlaubsverwaltung.settings;

import org.junit.Assert;
import org.junit.jupiter.api.Test;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.settings.Settings}.
 */
class SettingsTest {

    @Test
    void ensureDefaultValues() {

        Settings settings = new Settings();

        Assert.assertNotNull("Should not be null", settings.getAbsenceSettings());
        Assert.assertNotNull("Should not be null", settings.getWorkingTimeSettings());
        Assert.assertNotNull("Should not be null", settings.getCalendarSettings());
    }
}
