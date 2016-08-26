package org.synyx.urlaubsverwaltung.web.settings;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.ExchangeCalendarSettings;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class CalendarSettingsValidatorTest {

    private Validator validator;

    @Before
    public void setUp() throws Exception {

        validator = new CalendarSettingsValidator();
    }


    // Supported class -------------------------------------------------------------------------------------------------

    @Test
    public void ensureCalendarSettingsClassIsSupported() throws Exception {

        Assert.assertTrue("Should support CalendarSettings class", validator.supports(CalendarSettings.class));
    }


    @Test
    public void ensureOtherClassThanCalendarSettingsIsNotSupported() throws Exception {

        Assert.assertFalse("Should not support other classes", validator.supports(Object.class));
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThatValidateFailsWithOtherClassThanCalendarSettings() throws Exception {

        Object o = new Object();
        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(o, mockError);
    }


    // Validation ------------------------------------------------------------------------------------------------------

    // Calendar settings -----------------------------------------------------------------------------------------------

    @Test
    public void ensureCalendarSettingsAreMandatory() {

        CalendarSettings calendarSettings = new CalendarSettings();
        calendarSettings.setWorkDayBeginHour(null);
        calendarSettings.setWorkDayEndHour(null);

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(calendarSettings, mockError);
        Mockito.verify(mockError).rejectValue("workDayBeginHour", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("workDayEndHour", "error.entry.mandatory");
    }


    @Test
    public void ensureCalendarSettingsAreInvalidIfWorkDayBeginAndEndHoursAreSame() {

        CalendarSettings calendarSettings = new CalendarSettings();
        calendarSettings.setWorkDayBeginHour(8);
        calendarSettings.setWorkDayEndHour(8);

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(calendarSettings, mockError);
        Mockito.verify(mockError).rejectValue("workDayBeginHour", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("workDayEndHour", "error.entry.invalid");
    }


    @Test
    public void ensureCalendarSettingsAreInvalidIfWorkDayBeginHourIsGreaterThanEndHour() {

        CalendarSettings calendarSettings = new CalendarSettings();
        calendarSettings.setWorkDayBeginHour(17);
        calendarSettings.setWorkDayEndHour(8);

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(calendarSettings, mockError);
        Mockito.verify(mockError).rejectValue("workDayBeginHour", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("workDayEndHour", "error.entry.invalid");
    }


    @Test
    public void ensureCalendarSettingsAreInvalidIfWorkDayBeginOrEndHoursAreNegative() {

        CalendarSettings calendarSettings = new CalendarSettings();
        calendarSettings.setWorkDayBeginHour(-1);
        calendarSettings.setWorkDayEndHour(-2);

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(calendarSettings, mockError);
        Mockito.verify(mockError).rejectValue("workDayBeginHour", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("workDayEndHour", "error.entry.invalid");
    }


    @Test
    public void ensureCalendarSettingsAreInvalidIfWorkDayBeginOrEndHoursAreZero() {

        CalendarSettings calendarSettings = new CalendarSettings();
        calendarSettings.setWorkDayBeginHour(0);
        calendarSettings.setWorkDayEndHour(0);

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(calendarSettings, mockError);
        Mockito.verify(mockError).rejectValue("workDayBeginHour", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("workDayEndHour", "error.entry.invalid");
    }


    @Test
    public void ensureCalendarSettingsAreInvalidIfWorkDayBeginOrEndHoursAreGreaterThan24() {

        CalendarSettings calendarSettings = new CalendarSettings();
        calendarSettings.setWorkDayBeginHour(25);
        calendarSettings.setWorkDayEndHour(42);

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(calendarSettings, mockError);
        Mockito.verify(mockError).rejectValue("workDayBeginHour", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("workDayEndHour", "error.entry.invalid");
    }


    @Test
    public void ensureCalendarSettingsAreValidIfValidWorkDayBeginAndEndHours() {

        CalendarSettings calendarSettings = new CalendarSettings();
        calendarSettings.setWorkDayBeginHour(10);
        calendarSettings.setWorkDayEndHour(18);

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(calendarSettings, mockError);

        Mockito.verifyZeroInteractions(mockError);
    }


    // Exchange calendar settings --------------------------------------------------------------------------------------

    @Test
    public void ensureExchangeCalendarSettingsAreNotMandatoryIfDeactivated() {

        CalendarSettings calendarSettings = new CalendarSettings();
        ExchangeCalendarSettings exchangeCalendarSettings = calendarSettings.getExchangeCalendarSettings();

        exchangeCalendarSettings.setActive(false);
        exchangeCalendarSettings.setEmail(null);
        exchangeCalendarSettings.setPassword(null);
        exchangeCalendarSettings.setCalendar(null);

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(calendarSettings, mockError);

        Mockito.verifyZeroInteractions(mockError);
    }


    @Test
    public void ensureExchangeCalendarSettingsAreMandatoryIfActivated() {

        CalendarSettings calendarSettings = new CalendarSettings();
        ExchangeCalendarSettings exchangeCalendarSettings = calendarSettings.getExchangeCalendarSettings();

        exchangeCalendarSettings.setActive(true);
        exchangeCalendarSettings.setEmail(null);
        exchangeCalendarSettings.setPassword(null);
        exchangeCalendarSettings.setCalendar(null);

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(calendarSettings, mockError);

        Mockito.verify(mockError).rejectValue("exchangeCalendarSettings.email", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("exchangeCalendarSettings.password", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("exchangeCalendarSettings.calendar", "error.entry.mandatory");
    }


    @Test
    public void ensureExchangeCalendarEmailMustHaveValidFormat() {

        CalendarSettings calendarSettings = new CalendarSettings();
        ExchangeCalendarSettings exchangeCalendarSettings = calendarSettings.getExchangeCalendarSettings();

        exchangeCalendarSettings.setActive(true);
        exchangeCalendarSettings.setEmail("synyx");
        exchangeCalendarSettings.setPassword("top-secret");
        exchangeCalendarSettings.setCalendar("Urlaub");

        Errors mockError = Mockito.mock(Errors.class);
        validator.validate(calendarSettings, mockError);

        Mockito.verify(mockError).rejectValue("exchangeCalendarSettings.email", "error.entry.mail");
    }
}
