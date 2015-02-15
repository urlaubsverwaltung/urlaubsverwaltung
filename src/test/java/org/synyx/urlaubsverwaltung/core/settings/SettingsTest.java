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

        Assert.assertEquals("Wrong default value", (Integer) 40, settings.getMaximumAnnualVacationDays());
        Assert.assertEquals("Wrong default value", (Integer) 12, settings.getMaximumMonthsToApplyForLeaveInAdvance());
        Assert.assertEquals("Wrong default value", (Integer) 42, settings.getMaximumSickPayDays());
        Assert.assertEquals("Wrong default value", (Integer) 7, settings.getDaysBeforeEndOfSickPayNotification());
        Assert.assertEquals("Wrong default value", DayLength.MORNING, settings.getWorkingDurationForChristmasEve());
        Assert.assertEquals("Wrong default value", DayLength.MORNING, settings.getWorkingDurationForNewYearsEve());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingMaximumAnnualVacationDaysToNull() {

        new Settings().setMaximumAnnualVacationDays(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingMaximumAnnualVacationDaysToNegativeValue() {

        new Settings().setMaximumAnnualVacationDays(-1);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingMaximumAnnualVacationDaysToZero() {

        new Settings().setMaximumAnnualVacationDays(0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingMaximumAnnualVacationDaysToValueGreaterThanNumberOfDaysInOneYear() {

        new Settings().setMaximumAnnualVacationDays(367);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingMaximumMonthsToApplyForLeaveInAdvanceToNull() {

        new Settings().setMaximumMonthsToApplyForLeaveInAdvance(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingMaximumMonthsToApplyForLeaveInAdvanceToNegativeValue() {

        new Settings().setMaximumMonthsToApplyForLeaveInAdvance(-1);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingMaximumMonthsToApplyForLeaveInAdvanceToZero() {

        new Settings().setMaximumMonthsToApplyForLeaveInAdvance(0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingMaximumSickPayDaysToNegativeNull() {

        new Settings().setMaximumSickPayDays(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingMaximumSickPayDaysToNegativeValue() {

        new Settings().setMaximumSickPayDays(-1);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingMaximumSickPayDaysToZero() {

        new Settings().setMaximumSickPayDays(0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingDaysBeforeEndOfSickPayNotificationToNull() {

        new Settings().setDaysBeforeEndOfSickPayNotification(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingDaysBeforeEndOfSickPayNotificationToNegativeValue() {

        new Settings().setDaysBeforeEndOfSickPayNotification(-1);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingDaysBeforeEndOfSickPayNotificationToZero() {

        new Settings().setDaysBeforeEndOfSickPayNotification(0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingDaysBeforeEndOfSickPayNotificationWithoutToHaveDefinedMaximumSickPayDays() {

        Settings settings = new Settings();
        settings.setMaximumSickPayDays(null);
        settings.setDaysBeforeEndOfSickPayNotification(7);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingDaysBeforeEndOfSickPayNotificationToGreaterValueThanMaximumSickPayDays() {

        Settings settings = new Settings();
        settings.setMaximumSickPayDays(42);
        settings.setDaysBeforeEndOfSickPayNotification(43);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingDaysBeforeEndOfSickPayNotificationToEqualValueOfMaximumSickPayDays() {

        Settings settings = new Settings();
        settings.setMaximumSickPayDays(42);
        settings.setDaysBeforeEndOfSickPayNotification(42);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingWorkingDurationForChristmasEveToNull() {

        new Settings().setWorkingDurationForChristmasEve(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfSettingWorkingDurationForNewYearsEveToNull() {

        new Settings().setWorkingDurationForNewYearsEve(null);
    }
}
