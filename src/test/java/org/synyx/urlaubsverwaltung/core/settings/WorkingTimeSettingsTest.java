package org.synyx.urlaubsverwaltung.core.settings;

import org.junit.Assert;
import org.junit.Test;

import org.synyx.urlaubsverwaltung.core.period.DayLength;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class WorkingTimeSettingsTest {

    @Test
    public void ensureDefaultValues() {

        WorkingTimeSettings settings = new WorkingTimeSettings();

        Assert.assertNotNull("Should not be null", settings.getWorkingDurationForChristmasEve());
        Assert.assertNotNull("Should not be null", settings.getWorkingDurationForNewYearsEve());
        Assert.assertNotNull("Should not be null", settings.getFederalState());
        Assert.assertNotNull("Should not be null", settings.getMaximumOvertime());

        Assert.assertEquals("Wrong default value", DayLength.MORNING, settings.getWorkingDurationForChristmasEve());
        Assert.assertEquals("Wrong default value", DayLength.MORNING, settings.getWorkingDurationForNewYearsEve());
        Assert.assertEquals("Wrong default value", FederalState.BADEN_WUERTTEMBERG, settings.getFederalState());
        Assert.assertEquals("Wrong default value", (Integer) 100, settings.getMaximumOvertime());
    }
}
