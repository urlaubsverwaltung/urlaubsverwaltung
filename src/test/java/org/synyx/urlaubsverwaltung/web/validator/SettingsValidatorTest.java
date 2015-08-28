package org.synyx.urlaubsverwaltung.web.validator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.validation.Errors;

import org.synyx.urlaubsverwaltung.core.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.ExchangeCalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.MailSettings;
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
            .rejectValue("daysBeforeEndOfSickPayNotification",
                "settings.sickDays.daysBeforeEndOfSickPayNotification.error");
    }


    @Test
    public void ensureMailSettingsAreNotMandatoryIfDeactivated() {

        Settings settings = new Settings();
        MailSettings mailSettings = new MailSettings();
        settings.setMailSettings(mailSettings);

        mailSettings.setActive(false);
        mailSettings.setHost(null);
        mailSettings.setPort(null);
        mailSettings.setAdministrator(null);
        mailSettings.setFrom(null);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);

        Mockito.verifyZeroInteractions(mockError);
    }


    @Test
    public void ensureMandatoryMailSettingsAreMandatoryIfActivated() {

        Settings settings = new Settings();
        MailSettings mailSettings = new MailSettings();
        settings.setMailSettings(mailSettings);

        mailSettings.setActive(true);
        mailSettings.setHost(null);
        mailSettings.setPort(null);
        mailSettings.setAdministrator(null);
        mailSettings.setFrom(null);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        Mockito.verify(mockError).rejectValue("mailSettings.host", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("mailSettings.port", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("mailSettings.administrator", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("mailSettings.from", "error.entry.mandatory");
    }


    @Test
    public void ensureFromAndAdministratorMailAddressesMustBeValid() {

        Settings settings = new Settings();
        MailSettings mailSettings = new MailSettings();
        settings.setMailSettings(mailSettings);

        mailSettings.setAdministrator("foo");
        mailSettings.setFrom("bar");

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        Mockito.verify(mockError).rejectValue("mailSettings.administrator", "error.entry.mail");
        Mockito.verify(mockError).rejectValue("mailSettings.from", "error.entry.mail");
    }


    @Test
    public void ensureMailPortMustBeNotNegative() {

        Settings settings = new Settings();
        MailSettings mailSettings = new MailSettings();
        settings.setMailSettings(mailSettings);

        mailSettings.setPort(-1);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        Mockito.verify(mockError).rejectValue("mailSettings.port", "error.entry.invalid");
    }


    @Test
    public void ensureMailPortMustBeGreaterThanZero() {

        Settings settings = new Settings();
        MailSettings mailSettings = new MailSettings();
        settings.setMailSettings(mailSettings);

        mailSettings.setPort(0);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        Mockito.verify(mockError).rejectValue("mailSettings.port", "error.entry.invalid");
    }


    @Test
    public void ensureCalendarSettingsAreMandatory() {

        Settings settings = new Settings();
        CalendarSettings calendarSettings = settings.getCalendarSettings();

        calendarSettings.setWorkDayBeginHour(null);
        calendarSettings.setWorkDayEndHour(null);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        Mockito.verify(mockError).rejectValue("calendarSettings.workDayBeginHour", "error.entry.mandatory");
        Mockito.verify(mockError).rejectValue("calendarSettings.workDayEndHour", "error.entry.mandatory");
    }


    @Test
    public void ensureCalendarSettingsAreInvalidIfWorkDayBeginAndEndHoursAreSame() {

        Settings settings = new Settings();
        CalendarSettings calendarSettings = settings.getCalendarSettings();

        calendarSettings.setWorkDayBeginHour(8);
        calendarSettings.setWorkDayEndHour(8);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        Mockito.verify(mockError).rejectValue("calendarSettings.workDayBeginHour", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("calendarSettings.workDayEndHour", "error.entry.invalid");
    }


    @Test
    public void ensureCalendarSettingsAreInvalidIfWorkDayBeginHourIsGreaterThanEndHour() {

        Settings settings = new Settings();
        CalendarSettings calendarSettings = settings.getCalendarSettings();

        calendarSettings.setWorkDayBeginHour(17);
        calendarSettings.setWorkDayEndHour(8);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        Mockito.verify(mockError).rejectValue("calendarSettings.workDayBeginHour", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("calendarSettings.workDayEndHour", "error.entry.invalid");
    }


    @Test
    public void ensureCalendarSettingsAreInvalidIfWorkDayBeginOrEndHoursAreNegative() {

        Settings settings = new Settings();
        CalendarSettings calendarSettings = settings.getCalendarSettings();

        calendarSettings.setWorkDayBeginHour(-1);
        calendarSettings.setWorkDayEndHour(-2);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        Mockito.verify(mockError).rejectValue("calendarSettings.workDayBeginHour", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("calendarSettings.workDayEndHour", "error.entry.invalid");
    }


    @Test
    public void ensureCalendarSettingsAreInvalidIfWorkDayBeginOrEndHoursAreZero() {

        Settings settings = new Settings();
        CalendarSettings calendarSettings = settings.getCalendarSettings();

        calendarSettings.setWorkDayBeginHour(0);
        calendarSettings.setWorkDayEndHour(0);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        Mockito.verify(mockError).rejectValue("calendarSettings.workDayBeginHour", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("calendarSettings.workDayEndHour", "error.entry.invalid");
    }


    @Test
    public void ensureCalendarSettingsAreInvalidIfWorkDayBeginOrEndHoursAreGreaterThan24() {

        Settings settings = new Settings();
        CalendarSettings calendarSettings = settings.getCalendarSettings();

        calendarSettings.setWorkDayBeginHour(25);
        calendarSettings.setWorkDayEndHour(42);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        Mockito.verify(mockError).rejectValue("calendarSettings.workDayBeginHour", "error.entry.invalid");
        Mockito.verify(mockError).rejectValue("calendarSettings.workDayEndHour", "error.entry.invalid");
    }


    @Test
    public void ensureCalendarSettingsAreValidIfValidWorkDayBeginAndEndHours() {

        Settings settings = new Settings();
        CalendarSettings calendarSettings = settings.getCalendarSettings();

        calendarSettings.setWorkDayBeginHour(10);
        calendarSettings.setWorkDayEndHour(18);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);

        Mockito.verifyZeroInteractions(mockError);
    }


    @Test
    public void ensureExchangeCalendarSettingsAreNotMandatoryIfDeactivated() {

        Settings settings = new Settings();
        ExchangeCalendarSettings exchangeCalendarSettings = settings.getCalendarSettings()
            .getExchangeCalendarSettings();

        exchangeCalendarSettings.setActive(false);
        exchangeCalendarSettings.setEmail(null);
        exchangeCalendarSettings.setPassword(null);
        exchangeCalendarSettings.setCalendar(null);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);

        Mockito.verifyZeroInteractions(mockError);
    }


    @Test
    public void ensureExchangeCalendarSettingsAreMandatoryIfActivated() {

        Settings settings = new Settings();
        ExchangeCalendarSettings exchangeCalendarSettings = settings.getCalendarSettings()
            .getExchangeCalendarSettings();

        exchangeCalendarSettings.setActive(true);
        exchangeCalendarSettings.setEmail(null);
        exchangeCalendarSettings.setPassword(null);
        exchangeCalendarSettings.setCalendar(null);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);

        Mockito.verify(mockError)
            .rejectValue("calendarSettings.exchangeCalendarSettings.email", "error.entry.mandatory");
        Mockito.verify(mockError)
            .rejectValue("calendarSettings.exchangeCalendarSettings.password", "error.entry.mandatory");
        Mockito.verify(mockError)
            .rejectValue("calendarSettings.exchangeCalendarSettings.calendar", "error.entry.mandatory");
    }


    @Test
    public void ensureExchangeCalendarSettingsHaveValidMailAddress() {

        Settings settings = new Settings();
        ExchangeCalendarSettings exchangeCalendarSettings = settings.getCalendarSettings()
            .getExchangeCalendarSettings();

        exchangeCalendarSettings.setActive(true);
        exchangeCalendarSettings.setEmail("foo");
        exchangeCalendarSettings.setPassword("bar");
        exchangeCalendarSettings.setCalendar("calendar");

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);

        Mockito.verify(mockError).rejectValue("calendarSettings.exchangeCalendarSettings.email", "error.entry.mail");
    }
}
