package org.synyx.urlaubsverwaltung.settings;

import org.junit.Assert;
import org.junit.jupiter.api.Test;


class AbsenceSettingsTest {

    @Test
    void ensureDefaultValues() {

        AbsenceSettings settings = new AbsenceSettings();

        Assert.assertNotNull("Should not be null", settings.getMaximumAnnualVacationDays());
        Assert.assertNotNull("Should not be null", settings.getMaximumMonthsToApplyForLeaveInAdvance());
        Assert.assertNotNull("Should not be null", settings.getMaximumSickPayDays());
        Assert.assertNotNull("Should not be null", settings.getDaysBeforeEndOfSickPayNotification());

        Assert.assertEquals("Wrong default value", (Integer) 40, settings.getMaximumAnnualVacationDays());
        Assert.assertEquals("Wrong default value", (Integer) 12, settings.getMaximumMonthsToApplyForLeaveInAdvance());
        Assert.assertEquals("Wrong default value", (Integer) 42, settings.getMaximumSickPayDays());
        Assert.assertEquals("Wrong default value", (Integer) 7, settings.getDaysBeforeEndOfSickPayNotification());
    }
}
