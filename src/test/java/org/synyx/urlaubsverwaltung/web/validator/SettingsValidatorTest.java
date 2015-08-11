package org.synyx.urlaubsverwaltung.web.validator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.validation.Errors;

import org.synyx.urlaubsverwaltung.core.settings.Settings;

import static org.junit.Assert.*;


/**
 * Daniel Hammann - <hammann@synyx.de>.
 */
public class SettingsValidatorTest {

    private SettingsValidator settingsValidator;

    @Before
    public void setUp() throws Exception {

        settingsValidator = new SettingsValidator();
    }


    @Test
    public void ensureSettingsClassIsSupported() throws Exception {

        Assert.assertTrue("Should return true for Settings class.", settingsValidator.supports(Settings.class));
    }


    @Test
    public void ensureOtherClassThanSettingsIsNotSupported() throws Exception {

        Assert.assertFalse("Should return false for other classes than Settings.",
            settingsValidator.supports(Object.class));
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThatValidateFailsWithOtherClassThanSettings() throws Exception {

        Object o = new Object();
        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(o, mockError);
    }


    @Test
    public void ensureThatMembersNotNull() throws Exception {

        Settings settings = new Settings();
        settings.setFederalState(null);
        settings.setDaysBeforeEndOfSickPayNotification(null);
        settings.setMaximumAnnualVacationDays(null);
        settings.setMaximumMonthsToApplyForLeaveInAdvance(null);
        settings.setMaximumSickPayDays(null);
        settings.setWorkingDurationForChristmasEve(null);
        settings.setWorkingDurationForNewYearsEve(null);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        Mockito.verify(mockError).rejectValue("federalState", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("daysBeforeEndOfSickPayNotification", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("maximumAnnualVacationDays", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("maximumMonthsToApplyForLeaveInAdvance", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("maximumSickPayDays", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("workingDurationForChristmasEve", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("workingDurationForNewYearsEve", "error.entry.mandatory");
    }


    @Test
    public void ensureThatNumberMembersNotNegative() throws Exception {

        Settings settings = new Settings();
        settings.setDaysBeforeEndOfSickPayNotification(-1);
        settings.setMaximumAnnualVacationDays(-1);
        settings.setMaximumMonthsToApplyForLeaveInAdvance(-1);
        settings.setMaximumSickPayDays(-1);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        Mockito.verify(mockError).rejectValue("daysBeforeEndOfSickPayNotification", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("maximumAnnualVacationDays", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("maximumMonthsToApplyForLeaveInAdvance", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("maximumSickPayDays", "error.entry.invalid");
    }


    @Test
    public void ensureThatMaximumMonthsToApplyForLeaveInAdvanceNotZero() throws Exception {

        Settings settings = new Settings();
        settings.setMaximumMonthsToApplyForLeaveInAdvance(0);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        Mockito.verify(mockError).rejectValue("maximumMonthsToApplyForLeaveInAdvance", "error.entry.invalid");
    }


    @Test
    public void ensureThatMaximumAnnualVacationDaysSmallerThanAYear() throws Exception {

        Settings settings = new Settings();
        settings.setMaximumAnnualVacationDays(367);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        Mockito.verify(mockError).rejectValue("maximumAnnualVacationDays", "error.entry.invalid");
    }


    @Test
    public void ensureThatNumbersAreSmallerOrEqualsThanMaxInt() throws Exception {

        Settings settings = new Settings();
        settings.setDaysBeforeEndOfSickPayNotification(Integer.MAX_VALUE + 1);
        settings.setMaximumAnnualVacationDays(Integer.MAX_VALUE + 1);
        settings.setMaximumMonthsToApplyForLeaveInAdvance(Integer.MAX_VALUE + 1);
        settings.setMaximumSickPayDays(Integer.MAX_VALUE + 1);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        Mockito.verify(mockError).rejectValue("daysBeforeEndOfSickPayNotification", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("maximumAnnualVacationDays", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("maximumMonthsToApplyForLeaveInAdvance", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("maximumSickPayDays", "error.entry.invalid");
    }


    @Test
    public void ensureThatDaysBeforeEndOfSickPayNotificationIsSmallerThanMaximumSickPayDays() throws Exception {

        Settings settings = new Settings();
        settings.setDaysBeforeEndOfSickPayNotification(11);
        settings.setMaximumSickPayDays(10);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        Mockito.verify(mockError)
            .rejectValue("daysBeforeEndOfSickPayNotification", "settings.daysBeforeEndOfSickPayNotification.error");
    }
}
