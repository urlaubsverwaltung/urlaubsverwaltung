package org.synyx.urlaubsverwaltung.web.settings;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.settings.WorkingTimeSettings;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class WorkingTimeSettingsValidatorTest {

    private Validator validator;

    @Before
    public void setUp() throws Exception {

        validator = new WorkingTimeSettingsValidator();
    }


    // Supported class -------------------------------------------------------------------------------------------------

    @Test
    public void ensureWorkingTimeSettingsClassIsSupported() throws Exception {

        Assert.assertTrue("Should support WorkingTimeSettings class", validator.supports(WorkingTimeSettings.class));
    }


    @Test
    public void ensureOtherClassThanWorkingTimeSettingsIsNotSupported() throws Exception {

        Assert.assertFalse("Should not support other classes", validator.supports(Object.class));
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThatValidateFailsWithOtherClassThanWorkingTimeSettings() throws Exception {

        Object o = new Object();
        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(o, mockError);
    }


    // Validation ------------------------------------------------------------------------------------------------------

    // Working time settings: Public holidays --------------------------------------------------------------------------

    @Test
    public void ensureWorkingTimeSettingsCanNotBeNull() throws Exception {

        WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setFederalState(null);
        workingTimeSettings.setWorkingDurationForChristmasEve(null);
        workingTimeSettings.setWorkingDurationForNewYearsEve(null);
        workingTimeSettings.setMaximumOvertime(null);

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(workingTimeSettings, mockError);
        Mockito.verify(mockError).rejectValue("federalState", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("workingDurationForChristmasEve", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("workingDurationForNewYearsEve", "error.entry.mandatory");
    }


    // Working time settings: Overtime settings ------------------------------------------------------------------------

    @Test
    public void ensureOvertimeSettingsAreMandatoryIfOvertimeManagementIsActive() {

        WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setOvertimeActive(true);
        workingTimeSettings.setMaximumOvertime(null);
        workingTimeSettings.setMinimumOvertime(null);

        Errors mockError = Mockito.mock(Errors.class);

        validator.validate(workingTimeSettings, mockError);

        Mockito.verify(mockError).rejectValue("maximumOvertime", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("minimumOvertime", "error.entry.mandatory");
    }


    @Test
    public void ensureOvertimeSettingsAreNotMandatoryIfOvertimeManagementIsInactive() {

        WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setOvertimeActive(false);
        workingTimeSettings.setMaximumOvertime(null);
        workingTimeSettings.setMinimumOvertime(null);

        Errors mockError = Mockito.mock(Errors.class);

        validator.validate(workingTimeSettings, mockError);

        Mockito.verifyZeroInteractions(mockError);
    }


    @Test
    public void ensureMaximumOvertimeCanNotBeNegative() {

        WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setOvertimeActive(true);
        workingTimeSettings.setMaximumOvertime(-1);

        Errors mockError = Mockito.mock(Errors.class);

        validator.validate(workingTimeSettings, mockError);

        Mockito.verify(mockError).rejectValue("maximumOvertime", "error.entry.invalid");
    }


    @Test
    public void ensureMinimumOvertimeCanNotBeNegative() {

        WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setOvertimeActive(true);
        workingTimeSettings.setMinimumOvertime(-1);

        Errors mockError = Mockito.mock(Errors.class);

        validator.validate(workingTimeSettings, mockError);

        Mockito.verify(mockError).rejectValue("minimumOvertime", "error.entry.invalid");
    }
}
