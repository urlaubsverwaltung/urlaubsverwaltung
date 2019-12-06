package org.synyx.urlaubsverwaltung.settings;

import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;


public class WorkingTimeSettingsTest {

    @Test
    public void ensureDefaultValues() {

        WorkingTimeSettings settings = new WorkingTimeSettings();

        // Public holidays ---------------------------------------------------------------------------------------------
        Assert.assertNotNull("Should not be null", settings.getWorkingDurationForChristmasEve());
        Assert.assertNotNull("Should not be null", settings.getWorkingDurationForNewYearsEve());
        Assert.assertNotNull("Should not be null", settings.getFederalState());

        Assert.assertEquals("Wrong default value", DayLength.MORNING, settings.getWorkingDurationForChristmasEve());
        Assert.assertEquals("Wrong default value", DayLength.MORNING, settings.getWorkingDurationForNewYearsEve());
        Assert.assertEquals("Wrong default value", FederalState.BADEN_WUERTTEMBERG, settings.getFederalState());

        // Overtime ----------------------------------------------------------------------------------------------------
        Assert.assertNotNull("Should not be null", settings.isOvertimeActive());
        Assert.assertNotNull("Should not be null", settings.getMaximumOvertime());
        Assert.assertNotNull("Should not be null", settings.getMinimumOvertime());

        Assert.assertFalse("Should be deactivated", settings.isOvertimeActive());
        Assert.assertEquals("Wrong default value", (Integer) 100, settings.getMaximumOvertime());
        Assert.assertEquals("Wrong default value", (Integer) 5, settings.getMinimumOvertime());
    }
}
