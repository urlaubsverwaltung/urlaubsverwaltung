package org.synyx.urlaubsverwaltung.web.settings;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.core.settings.*;
import org.synyx.urlaubsverwaltung.core.sync.providers.exchange.ExchangeCalendarProvider;
import org.synyx.urlaubsverwaltung.core.sync.providers.google.GoogleCalendarSyncProvider;

import static org.mockito.Mockito.verify;


/**
 * @author  Daniel Hammann - <hammann@synyx.de>
 * @author  Aljona Murygina - <murygina@synyx.de>
 */
public class SettingsValidatorTest {

    private SettingsValidator settingsValidator;

    @Before
    public void setUp() throws Exception {

        settingsValidator = new SettingsValidator();
    }


    // Supported class -------------------------------------------------------------------------------------------------

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


    // Working time settings: Public holidays --------------------------------------------------------------------------

    @Test
    public void ensureWorkingTimeSettingsCanNotBeNull() throws Exception {

        Settings settings = new Settings();
        WorkingTimeSettings workingTimeSettings = settings.getWorkingTimeSettings();
        workingTimeSettings.setFederalState(null);
        workingTimeSettings.setWorkingDurationForChristmasEve(null);
        workingTimeSettings.setWorkingDurationForNewYearsEve(null);
        workingTimeSettings.setMaximumOvertime(null);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        verify(mockError).rejectValue("workingTimeSettings.federalState", "error.entry.mandatory");
        verify(mockError)
            .rejectValue("workingTimeSettings.workingDurationForChristmasEve", "error.entry.mandatory");
        verify(mockError)
            .rejectValue("workingTimeSettings.workingDurationForNewYearsEve", "error.entry.mandatory");
    }


    // Working time settings: Overtime settings ------------------------------------------------------------------------

    @Test
    public void ensureOvertimeSettingsAreMandatoryIfOvertimeManagementIsActive() {

        Settings settings = new Settings();
        WorkingTimeSettings workingTimeSettings = settings.getWorkingTimeSettings();
        workingTimeSettings.setOvertimeActive(true);
        workingTimeSettings.setMaximumOvertime(null);
        workingTimeSettings.setMinimumOvertime(null);

        Errors mockError = Mockito.mock(Errors.class);

        settingsValidator.validate(settings, mockError);

        verify(mockError).rejectValue("workingTimeSettings.maximumOvertime", "error.entry.mandatory");
        verify(mockError).rejectValue("workingTimeSettings.minimumOvertime", "error.entry.mandatory");
    }


    @Test
    public void ensureOvertimeSettingsAreNotMandatoryIfOvertimeManagementIsInactive() {

        Settings settings = new Settings();
        WorkingTimeSettings workingTimeSettings = settings.getWorkingTimeSettings();
        workingTimeSettings.setOvertimeActive(false);
        workingTimeSettings.setMaximumOvertime(null);
        workingTimeSettings.setMinimumOvertime(null);

        Errors mockError = Mockito.mock(Errors.class);

        settingsValidator.validate(settings, mockError);

        Mockito.verifyZeroInteractions(mockError);
    }


    @Test
    public void ensureMaximumOvertimeCanNotBeNegative() {

        Settings settings = new Settings();
        WorkingTimeSettings workingTimeSettings = settings.getWorkingTimeSettings();
        workingTimeSettings.setOvertimeActive(true);
        workingTimeSettings.setMaximumOvertime(-1);

        Errors mockError = Mockito.mock(Errors.class);

        settingsValidator.validate(settings, mockError);

        verify(mockError).rejectValue("workingTimeSettings.maximumOvertime", "error.entry.invalid");
    }


    @Test
    public void ensureMinimumOvertimeCanNotBeNegative() {

        Settings settings = new Settings();
        WorkingTimeSettings workingTimeSettings = settings.getWorkingTimeSettings();
        workingTimeSettings.setOvertimeActive(true);
        workingTimeSettings.setMinimumOvertime(-1);

        Errors mockError = Mockito.mock(Errors.class);

        settingsValidator.validate(settings, mockError);

        verify(mockError).rejectValue("workingTimeSettings.minimumOvertime", "error.entry.invalid");
    }


    // Absence settings ------------------------------------------------------------------------------------------------

    @Test
    public void ensureAbsenceSettingsCanNotBeNull() {

        Settings settings = new Settings();
        AbsenceSettings absenceSettings = settings.getAbsenceSettings();

        absenceSettings.setMaximumAnnualVacationDays(null);
        absenceSettings.setMaximumMonthsToApplyForLeaveInAdvance(null);
        absenceSettings.setMaximumSickPayDays(null);
        absenceSettings.setDaysBeforeEndOfSickPayNotification(null);
        absenceSettings.setDaysBeforeRemindForWaitingApplications(null);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        verify(mockError).rejectValue("absenceSettings.maximumAnnualVacationDays", "error.entry.mandatory");
        verify(mockError)
            .rejectValue("absenceSettings.maximumMonthsToApplyForLeaveInAdvance", "error.entry.mandatory");
        verify(mockError).rejectValue("absenceSettings.maximumSickPayDays", "error.entry.mandatory");
        verify(mockError)
            .rejectValue("absenceSettings.daysBeforeEndOfSickPayNotification", "error.entry.mandatory");
        verify(mockError)
                .rejectValue("absenceSettings.daysBeforeRemindForWaitingApplications", "error.entry.mandatory");
    }


    @Test
    public void ensureAbsenceSettingsCanNotBeNegative() {

        Settings settings = new Settings();
        AbsenceSettings absenceSettings = settings.getAbsenceSettings();

        absenceSettings.setMaximumAnnualVacationDays(-1);
        absenceSettings.setMaximumMonthsToApplyForLeaveInAdvance(-1);
        absenceSettings.setMaximumSickPayDays(-1);
        absenceSettings.setDaysBeforeEndOfSickPayNotification(-1);
        absenceSettings.setDaysBeforeRemindForWaitingApplications(-1);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        verify(mockError).rejectValue("absenceSettings.maximumAnnualVacationDays", "error.entry.invalid");
        verify(mockError)
            .rejectValue("absenceSettings.maximumMonthsToApplyForLeaveInAdvance", "error.entry.invalid");
        verify(mockError).rejectValue("absenceSettings.maximumSickPayDays", "error.entry.invalid");
        verify(mockError)
            .rejectValue("absenceSettings.daysBeforeEndOfSickPayNotification", "error.entry.invalid");
        verify(mockError)
                .rejectValue("absenceSettings.daysBeforeEndOfSickPayNotification", "error.entry.invalid");
    }


    @Test
    public void ensureThatMaximumMonthsToApplyForLeaveInAdvanceNotZero() throws Exception {

        Settings settings = new Settings();
        settings.getAbsenceSettings().setMaximumMonthsToApplyForLeaveInAdvance(0);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        verify(mockError)
            .rejectValue("absenceSettings.maximumMonthsToApplyForLeaveInAdvance", "error.entry.invalid");
    }


    @Test
    public void ensureThatMaximumAnnualVacationDaysSmallerThanAYear() throws Exception {

        Settings settings = new Settings();
        settings.getAbsenceSettings().setMaximumAnnualVacationDays(367);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        verify(mockError).rejectValue("absenceSettings.maximumAnnualVacationDays", "error.entry.invalid");
    }


    @Test
    public void ensureThatAbsenceSettingsAreSmallerOrEqualsThanMaxInt() throws Exception {

        Settings settings = new Settings();
        AbsenceSettings absenceSettings = settings.getAbsenceSettings();

        absenceSettings.setDaysBeforeEndOfSickPayNotification(Integer.MAX_VALUE + 1);
        absenceSettings.setMaximumAnnualVacationDays(Integer.MAX_VALUE + 1);
        absenceSettings.setMaximumMonthsToApplyForLeaveInAdvance(Integer.MAX_VALUE + 1);
        absenceSettings.setMaximumSickPayDays(Integer.MAX_VALUE + 1);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        verify(mockError)
            .rejectValue("absenceSettings.daysBeforeEndOfSickPayNotification", "error.entry.invalid");
        verify(mockError).rejectValue("absenceSettings.maximumAnnualVacationDays", "error.entry.invalid");
        verify(mockError)
            .rejectValue("absenceSettings.maximumMonthsToApplyForLeaveInAdvance", "error.entry.invalid");
        verify(mockError).rejectValue("absenceSettings.maximumSickPayDays", "error.entry.invalid");
    }


    @Test
    public void ensureThatDaysBeforeEndOfSickPayNotificationIsSmallerThanMaximumSickPayDays() throws Exception {

        Settings settings = new Settings();
        AbsenceSettings absenceSettings = settings.getAbsenceSettings();
        absenceSettings.setDaysBeforeEndOfSickPayNotification(11);
        absenceSettings.setMaximumSickPayDays(10);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        verify(mockError)
            .rejectValue("absenceSettings.daysBeforeEndOfSickPayNotification",
                "settings.sickDays.daysBeforeEndOfSickPayNotification.error");
    }


    // Mail settings ---------------------------------------------------------------------------------------------------

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
        mailSettings.setBaseLinkURL(null);

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
        mailSettings.setBaseLinkURL(null);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        verify(mockError).rejectValue("mailSettings.host", "error.entry.mandatory");
        verify(mockError).rejectValue("mailSettings.port", "error.entry.mandatory");
        verify(mockError).rejectValue("mailSettings.administrator", "error.entry.mandatory");
        verify(mockError).rejectValue("mailSettings.from", "error.entry.mandatory");
        verify(mockError).rejectValue("mailSettings.baseLinkURL", "error.entry.mandatory");
    }


    @Test
    public void ensureFromAndAdministratorMailAddressesMustBeValid() {

        Settings settings = new Settings();
        MailSettings mailSettings = new MailSettings();
        settings.setMailSettings(mailSettings);

        mailSettings.setActive(true);
        mailSettings.setAdministrator("foo");
        mailSettings.setFrom("bar");

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        verify(mockError).rejectValue("mailSettings.administrator", "error.entry.mail");
        verify(mockError).rejectValue("mailSettings.from", "error.entry.mail");
    }


    @Test
    public void ensureMailPortMustBeNotNegative() {

        Settings settings = new Settings();
        MailSettings mailSettings = new MailSettings();
        settings.setMailSettings(mailSettings);

        mailSettings.setActive(true);
        mailSettings.setPort(-1);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        verify(mockError).rejectValue("mailSettings.port", "error.entry.invalid");
    }


    @Test
    public void ensureMailPortMustBeGreaterThanZero() {

        Settings settings = new Settings();
        MailSettings mailSettings = new MailSettings();
        settings.setMailSettings(mailSettings);

        mailSettings.setActive(true);
        mailSettings.setPort(0);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        verify(mockError).rejectValue("mailSettings.port", "error.entry.invalid");
    }


    // Calendar settings -----------------------------------------------------------------------------------------------

    @Test
    public void ensureCalendarSettingsAreMandatory() {

        Settings settings = new Settings();
        CalendarSettings calendarSettings = settings.getCalendarSettings();

        calendarSettings.setWorkDayBeginHour(null);
        calendarSettings.setWorkDayEndHour(null);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        verify(mockError).rejectValue("calendarSettings.workDayBeginHour", "error.entry.mandatory");
        verify(mockError).rejectValue("calendarSettings.workDayEndHour", "error.entry.mandatory");
    }


    @Test
    public void ensureCalendarSettingsAreInvalidIfWorkDayBeginAndEndHoursAreSame() {

        Settings settings = new Settings();
        CalendarSettings calendarSettings = settings.getCalendarSettings();

        calendarSettings.setWorkDayBeginHour(8);
        calendarSettings.setWorkDayEndHour(8);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        verify(mockError).rejectValue("calendarSettings.workDayBeginHour", "error.entry.invalid");
        verify(mockError).rejectValue("calendarSettings.workDayEndHour", "error.entry.invalid");
    }


    @Test
    public void ensureCalendarSettingsAreInvalidIfWorkDayBeginHourIsGreaterThanEndHour() {

        Settings settings = new Settings();
        CalendarSettings calendarSettings = settings.getCalendarSettings();

        calendarSettings.setWorkDayBeginHour(17);
        calendarSettings.setWorkDayEndHour(8);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        verify(mockError).rejectValue("calendarSettings.workDayBeginHour", "error.entry.invalid");
        verify(mockError).rejectValue("calendarSettings.workDayEndHour", "error.entry.invalid");
    }


    @Test
    public void ensureCalendarSettingsAreInvalidIfWorkDayBeginOrEndHoursAreNegative() {

        Settings settings = new Settings();
        CalendarSettings calendarSettings = settings.getCalendarSettings();

        calendarSettings.setWorkDayBeginHour(-1);
        calendarSettings.setWorkDayEndHour(-2);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        verify(mockError).rejectValue("calendarSettings.workDayBeginHour", "error.entry.invalid");
        verify(mockError).rejectValue("calendarSettings.workDayEndHour", "error.entry.invalid");
    }


    @Test
    public void ensureCalendarSettingsAreInvalidIfWorkDayBeginOrEndHoursAreZero() {

        Settings settings = new Settings();
        CalendarSettings calendarSettings = settings.getCalendarSettings();

        calendarSettings.setWorkDayBeginHour(0);
        calendarSettings.setWorkDayEndHour(0);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        verify(mockError).rejectValue("calendarSettings.workDayBeginHour", "error.entry.invalid");
        verify(mockError).rejectValue("calendarSettings.workDayEndHour", "error.entry.invalid");
    }


    @Test
    public void ensureCalendarSettingsAreInvalidIfWorkDayBeginOrEndHoursAreGreaterThan24() {

        Settings settings = new Settings();
        CalendarSettings calendarSettings = settings.getCalendarSettings();

        calendarSettings.setWorkDayBeginHour(25);
        calendarSettings.setWorkDayEndHour(42);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);
        verify(mockError).rejectValue("calendarSettings.workDayBeginHour", "error.entry.invalid");
        verify(mockError).rejectValue("calendarSettings.workDayEndHour", "error.entry.invalid");
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


    // Exchange calendar settings --------------------------------------------------------------------------------------

    @Test
    public void ensureExchangeCalendarSettingsAreNotMandatoryIfDeactivated() {

        Settings settings = new Settings();
        ExchangeCalendarSettings exchangeCalendarSettings = settings.getCalendarSettings()
            .getExchangeCalendarSettings();

        exchangeCalendarSettings.setEmail(null);
        exchangeCalendarSettings.setPassword(null);
        exchangeCalendarSettings.setCalendar(null);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);

        Mockito.verifyZeroInteractions(mockError);
    }


    @Test
    public void ensureExchangeCalendarSettingsAreMandatory() {

        Settings settings = new Settings();
        ExchangeCalendarSettings exchangeCalendarSettings = settings.getCalendarSettings()
            .getExchangeCalendarSettings();

        settings.getCalendarSettings().setProvider(ExchangeCalendarProvider.class.getSimpleName());
        exchangeCalendarSettings.setEmail(null);
        exchangeCalendarSettings.setPassword(null);
        exchangeCalendarSettings.setCalendar(null);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);

        verify(mockError)
            .rejectValue("calendarSettings.exchangeCalendarSettings.email", "error.entry.mandatory");
        verify(mockError)
            .rejectValue("calendarSettings.exchangeCalendarSettings.password", "error.entry.mandatory");
        verify(mockError)
            .rejectValue("calendarSettings.exchangeCalendarSettings.calendar", "error.entry.mandatory");
    }

    @Test
    public void ensureExchangeCalendarEmailMustHaveValidFormat() {

        Settings settings = new Settings();
        ExchangeCalendarSettings exchangeCalendarSettings = settings.getCalendarSettings()
            .getExchangeCalendarSettings();

        settings.getCalendarSettings().setProvider(ExchangeCalendarProvider.class.getSimpleName());
        exchangeCalendarSettings.setEmail("synyx");
        exchangeCalendarSettings.setPassword("top-secret");
        exchangeCalendarSettings.setCalendar("Urlaub");

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);

        verify(mockError).rejectValue("calendarSettings.exchangeCalendarSettings.email", "error.entry.mail");
    }

    @Test
    public void ensureGoogleCalendarSettingsAreMandatory() {

        Settings settings = new Settings();
        GoogleCalendarSettings googleCalendarSettings = settings.getCalendarSettings()
                .getGoogleCalendarSettings();

        settings.getCalendarSettings().setProvider(GoogleCalendarSyncProvider.class.getSimpleName());
        googleCalendarSettings.setCalendarId(null);
        googleCalendarSettings.setClientId(null);
        googleCalendarSettings.setClientSecret(null);
        googleCalendarSettings.setRedirectBaseUrl(null);

        Errors mockError = Mockito.mock(Errors.class);
        settingsValidator.validate(settings, mockError);

        verify(mockError)
                .rejectValue("calendarSettings.googleCalendarSettings.calendarId", "error.entry.mandatory");
        verify(mockError)
                .rejectValue("calendarSettings.googleCalendarSettings.clientId", "error.entry.mandatory");
        verify(mockError)
                .rejectValue("calendarSettings.googleCalendarSettings.clientSecret", "error.entry.mandatory");
        verify(mockError)
                .rejectValue("calendarSettings.googleCalendarSettings.redirectBaseUrl", "error.entry.mandatory");
    }
}
