package org.synyx.urlaubsverwaltung.core.settings;

import org.junit.Assert;
import org.junit.Test;

import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.core.settings.Settings}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SettingsTest {

    @Test
    public void ensureDefaultValues() {

        Settings settings = new Settings();

        Assert.assertNotNull("Should not be null", settings.getMaximumAnnualVacationDays());
        Assert.assertNotNull("Should not be null", settings.getMaximumMonthsToApplyForLeaveInAdvance());
        Assert.assertNotNull("Should not be null", settings.getMaximumSickPayDays());
        Assert.assertNotNull("Should not be null", settings.getDaysBeforeEndOfSickPayNotification());
        Assert.assertNotNull("Should not be null", settings.getWorkingDurationForChristmasEve());
        Assert.assertNotNull("Should not be null", settings.getWorkingDurationForNewYearsEve());
        Assert.assertNotNull("Should not be null", settings.getFederalState());
        Assert.assertNotNull("Should not be null", settings.getMailSettings());

        Assert.assertEquals("Wrong default value", (Integer) 40, settings.getMaximumAnnualVacationDays());
        Assert.assertEquals("Wrong default value", (Integer) 12, settings.getMaximumMonthsToApplyForLeaveInAdvance());
        Assert.assertEquals("Wrong default value", (Integer) 42, settings.getMaximumSickPayDays());
        Assert.assertEquals("Wrong default value", (Integer) 7, settings.getDaysBeforeEndOfSickPayNotification());
        Assert.assertEquals("Wrong default value", DayLength.MORNING, settings.getWorkingDurationForChristmasEve());
        Assert.assertEquals("Wrong default value", DayLength.MORNING, settings.getWorkingDurationForNewYearsEve());
        Assert.assertEquals("Wrong default value", FederalState.BADEN_WUERTTEMBERG, settings.getFederalState());
    }
}
