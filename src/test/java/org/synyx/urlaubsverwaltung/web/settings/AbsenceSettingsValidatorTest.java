package org.synyx.urlaubsverwaltung.web.settings;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.settings.AbsenceSettings;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class AbsenceSettingsValidatorTest {

    private Validator validator;

    @Before
    public void setUp() throws Exception {

        validator = new AbsenceSettingsValidator();
    }


    // Supported class -------------------------------------------------------------------------------------------------

    @Test
    public void ensureAbsenceSettingsClassIsSupported() throws Exception {

        Assert.assertTrue("Should support AbsenceSettings class", validator.supports(AbsenceSettings.class));
    }


    @Test
    public void ensureOtherClassThanAbsenceSettingsIsNotSupported() throws Exception {

        Assert.assertFalse("Should not support other classes", validator.supports(Object.class));
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThatValidateFailsWithOtherClassThanAbsenceSettings() throws Exception {

        Object o = new Object();
        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(o, mockError);
    }


    // Validation ------------------------------------------------------------------------------------------------------

    @Test
    public void ensureAbsenceSettingsCanNotBeNull() {

        AbsenceSettings settings = new AbsenceSettings();

        settings.setMaximumAnnualVacationDays(null);
        settings.setMaximumMonthsToApplyForLeaveInAdvance(null);
        settings.setMaximumSickPayDays(null);
        settings.setDaysBeforeEndOfSickPayNotification(null);
        settings.setDaysBeforeRemindForWaitingApplications(null);

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(settings, mockError);
        Mockito.verify(mockError).rejectValue("maximumAnnualVacationDays", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("maximumMonthsToApplyForLeaveInAdvance", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("maximumSickPayDays", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("daysBeforeEndOfSickPayNotification", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("daysBeforeRemindForWaitingApplications", "error.entry.mandatory");
    }


    @Test
    public void ensureAbsenceSettingsCanNotBeNegative() {

        AbsenceSettings settings = new AbsenceSettings();
        settings.setMaximumAnnualVacationDays(-1);
        settings.setMaximumMonthsToApplyForLeaveInAdvance(-1);
        settings.setMaximumSickPayDays(-1);
        settings.setDaysBeforeEndOfSickPayNotification(-1);
        settings.setDaysBeforeRemindForWaitingApplications(-1);

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(settings, mockError);
        Mockito.verify(mockError).rejectValue("maximumAnnualVacationDays", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("maximumMonthsToApplyForLeaveInAdvance", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("maximumSickPayDays", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("daysBeforeEndOfSickPayNotification", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("daysBeforeEndOfSickPayNotification", "error.entry.invalid");
    }


    @Test
    public void ensureThatMaximumMonthsToApplyForLeaveInAdvanceNotZero() throws Exception {

        AbsenceSettings settings = new AbsenceSettings();
        settings.setMaximumMonthsToApplyForLeaveInAdvance(0);

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(settings, mockError);
        Mockito.verify(mockError).rejectValue("maximumMonthsToApplyForLeaveInAdvance", "error.entry.invalid");
    }


    @Test
    public void ensureThatMaximumAnnualVacationDaysSmallerThanAYear() throws Exception {

        AbsenceSettings settings = new AbsenceSettings();
        settings.setMaximumAnnualVacationDays(367);

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(settings, mockError);
        Mockito.verify(mockError).rejectValue("maximumAnnualVacationDays", "error.entry.invalid");
    }


    @Test
    public void ensureThatAbsenceSettingsAreSmallerOrEqualsThanMaxInt() throws Exception {

        AbsenceSettings settings = new AbsenceSettings();
        settings.setDaysBeforeEndOfSickPayNotification(Integer.MAX_VALUE + 1);
        settings.setMaximumAnnualVacationDays(Integer.MAX_VALUE + 1);
        settings.setMaximumMonthsToApplyForLeaveInAdvance(Integer.MAX_VALUE + 1);
        settings.setMaximumSickPayDays(Integer.MAX_VALUE + 1);

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(settings, mockError);
        Mockito.verify(mockError).rejectValue("daysBeforeEndOfSickPayNotification", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("maximumAnnualVacationDays", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("maximumMonthsToApplyForLeaveInAdvance", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("maximumSickPayDays", "error.entry.invalid");
    }


    @Test
    public void ensureThatDaysBeforeEndOfSickPayNotificationIsSmallerThanMaximumSickPayDays() throws Exception {

        AbsenceSettings settings = new AbsenceSettings();
        settings.setDaysBeforeEndOfSickPayNotification(11);
        settings.setMaximumSickPayDays(10);

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(settings, mockError);
        Mockito.verify(mockError)
            .rejectValue("daysBeforeEndOfSickPayNotification",
                "settings.sickDays.daysBeforeEndOfSickPayNotification.error");
    }
}
