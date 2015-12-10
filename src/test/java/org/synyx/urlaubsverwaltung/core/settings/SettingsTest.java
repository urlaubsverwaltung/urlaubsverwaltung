package org.synyx.urlaubsverwaltung.core.settings;

import org.junit.Assert;
import org.junit.Test;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.core.settings.Settings}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SettingsTest {

    @Test
    public void ensureDefaultValues() {

        Settings settings = new Settings();

        Assert.assertNotNull("Should not be null", settings.getAbsenceSettings());
        Assert.assertNotNull("Should not be null", settings.getWorkingTimeSettings());
        Assert.assertNotNull("Should not be null", settings.getMailSettings());
        Assert.assertNotNull("Should not be null", settings.getCalendarSettings());
    }
}
