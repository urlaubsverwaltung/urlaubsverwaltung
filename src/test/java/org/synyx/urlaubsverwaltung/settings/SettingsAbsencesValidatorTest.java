package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.account.AccountSettings;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;

import static java.lang.Integer.MAX_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SettingsAbsencesValidatorTest {

    private SettingsAbsencesValidator sut;

    @BeforeEach
    void setUp() {
        sut = new SettingsAbsencesValidator();
    }

    @Test
    void ensureSettingsClassIsSupported() {
        assertThat(sut.supports(SettingsAbsencesDto.class)).isTrue();
    }

    @Test
    void ensureOtherClassThanSettingsIsNotSupported() {
        assertThat(sut.supports(Object.class)).isFalse();
    }

    // Application settings ------------------------------------------------------------------------------------------------
    @Test
    void ensureApplicationSettingsCanNotBeNull() {

        final ApplicationSettings applicationSettings = new ApplicationSettings();
        applicationSettings.setMaximumMonthsToApplyForLeaveInAdvance(null);
        applicationSettings.setMaximumMonthsToApplyForLeaveAfterwards(null);
        applicationSettings.setDaysBeforeRemindForWaitingApplications(null);
        applicationSettings.setDaysBeforeRemindForUpcomingHolidayReplacement(null);
        applicationSettings.setDaysBeforeRemindForUpcomingApplications(null);

        final SettingsAbsencesDto dto = new SettingsAbsencesDto();
        dto.setApplicationSettings(applicationSettings);
        dto.setAccountSettings(new AccountSettings());
        dto.setSickNoteSettings(new SickNoteSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("applicationSettings.maximumMonthsToApplyForLeaveInAdvance", "error.entry.mandatory");
        verify(mockError).rejectValue("applicationSettings.maximumMonthsToApplyForLeaveAfterwards", "error.entry.mandatory");
        verify(mockError).rejectValue("applicationSettings.daysBeforeRemindForWaitingApplications", "error.entry.mandatory");
        verify(mockError).rejectValue("applicationSettings.daysBeforeRemindForUpcomingHolidayReplacement", "error.entry.mandatory");
        verify(mockError).rejectValue("applicationSettings.daysBeforeRemindForUpcomingApplications", "error.entry.mandatory");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void ensureApplicationSettingsIsInvalid(int invalidValue) {

        final ApplicationSettings applicationSettings = new ApplicationSettings();
        applicationSettings.setMaximumMonthsToApplyForLeaveInAdvance(invalidValue);
        applicationSettings.setMaximumMonthsToApplyForLeaveAfterwards(invalidValue);
        applicationSettings.setDaysBeforeRemindForWaitingApplications(invalidValue);
        applicationSettings.setDaysBeforeRemindForUpcomingHolidayReplacement(invalidValue);
        applicationSettings.setDaysBeforeRemindForUpcomingApplications(invalidValue);

        final SettingsAbsencesDto dto = new SettingsAbsencesDto();
        dto.setApplicationSettings(applicationSettings);
        dto.setAccountSettings(new AccountSettings());
        dto.setSickNoteSettings(new SickNoteSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("applicationSettings.maximumMonthsToApplyForLeaveInAdvance", "error.entry.invalid");
        verify(mockError).rejectValue("applicationSettings.maximumMonthsToApplyForLeaveAfterwards", "error.entry.invalid");
        verify(mockError).rejectValue("applicationSettings.daysBeforeRemindForWaitingApplications", "error.entry.invalid");
        verify(mockError).rejectValue("applicationSettings.daysBeforeRemindForUpcomingHolidayReplacement", "error.entry.invalid");
        verify(mockError).rejectValue("applicationSettings.daysBeforeRemindForUpcomingApplications", "error.entry.invalid");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void ensureApplicationSettingsDaysBeforeRemindForWaitingApplicationsIsInvalid(int invalidValue) {

        final ApplicationSettings applicationSettings = new ApplicationSettings();
        applicationSettings.setMaximumMonthsToApplyForLeaveInAdvance(invalidValue);
        applicationSettings.setMaximumMonthsToApplyForLeaveAfterwards(invalidValue);
        applicationSettings.setDaysBeforeRemindForWaitingApplications(invalidValue);
        applicationSettings.setDaysBeforeRemindForUpcomingHolidayReplacement(invalidValue);
        applicationSettings.setDaysBeforeRemindForUpcomingApplications(invalidValue);

        final SettingsAbsencesDto dto = new SettingsAbsencesDto();
        dto.setApplicationSettings(applicationSettings);
        dto.setAccountSettings(new AccountSettings());
        dto.setSickNoteSettings(new SickNoteSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("applicationSettings.maximumMonthsToApplyForLeaveInAdvance", "error.entry.invalid");
        verify(mockError).rejectValue("applicationSettings.maximumMonthsToApplyForLeaveAfterwards", "error.entry.invalid");
        verify(mockError).rejectValue("applicationSettings.daysBeforeRemindForWaitingApplications", "error.entry.invalid");
        verify(mockError).rejectValue("applicationSettings.daysBeforeRemindForUpcomingHolidayReplacement", "error.entry.invalid");
        verify(mockError).rejectValue("applicationSettings.daysBeforeRemindForUpcomingApplications", "error.entry.invalid");
    }

    @Test
    void ensureThatAbsenceSettingsAreSmallerOrEqualsThanMaxInt() {

        final ApplicationSettings applicationSettings = new ApplicationSettings();
        applicationSettings.setMaximumMonthsToApplyForLeaveInAdvance(MAX_VALUE + 1);
        applicationSettings.setMaximumMonthsToApplyForLeaveAfterwards(MAX_VALUE + 1);
        applicationSettings.setDaysBeforeRemindForWaitingApplications(MAX_VALUE + 1);
        applicationSettings.setDaysBeforeRemindForUpcomingHolidayReplacement(MAX_VALUE + 1);
        applicationSettings.setDaysBeforeRemindForUpcomingApplications(MAX_VALUE + 1);

        final SettingsAbsencesDto dto = new SettingsAbsencesDto();
        dto.setApplicationSettings(applicationSettings);
        dto.setAccountSettings(new AccountSettings());
        dto.setSickNoteSettings(new SickNoteSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("applicationSettings.maximumMonthsToApplyForLeaveInAdvance", "error.entry.invalid");
        verify(mockError).rejectValue("applicationSettings.maximumMonthsToApplyForLeaveAfterwards", "error.entry.invalid");
        verify(mockError).rejectValue("applicationSettings.daysBeforeRemindForWaitingApplications", "error.entry.invalid");
        verify(mockError).rejectValue("applicationSettings.daysBeforeRemindForUpcomingHolidayReplacement", "error.entry.invalid");
        verify(mockError).rejectValue("applicationSettings.daysBeforeRemindForUpcomingApplications", "error.entry.invalid");
    }

    // Account settings ------------------------------------------------------------------------------------------------
    @Test
    void ensureMaximumAnnualVacationDaysAccountSettingsCanNotBeNull() {

        final AccountSettings accountSettings = new AccountSettings();
        accountSettings.setMaximumAnnualVacationDays(null);

        final SettingsAbsencesDto dto = new SettingsAbsencesDto();
        dto.setAccountSettings(accountSettings);
        dto.setApplicationSettings(new ApplicationSettings());
        dto.setSickNoteSettings(new SickNoteSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("accountSettings.maximumAnnualVacationDays", "error.entry.mandatory");
    }

    @Test
    void ensureMaximumAnnualVacationDaysAccountSettingsCanNotBeNegative() {

        final AccountSettings accountSettings = new AccountSettings();
        accountSettings.setMaximumAnnualVacationDays(-1);

        final SettingsAbsencesDto dto = new SettingsAbsencesDto();
        dto.setAccountSettings(accountSettings);
        dto.setApplicationSettings(new ApplicationSettings());
        dto.setSickNoteSettings(new SickNoteSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("accountSettings.maximumAnnualVacationDays", "error.entry.invalid");
    }

    @Test
    void ensureDefaultVacationDaysAccountSettingsCanNotBeNull() {

        final AccountSettings accountSettings = new AccountSettings();
        accountSettings.setDefaultVacationDays(null);

        final SettingsAbsencesDto dto = new SettingsAbsencesDto();
        dto.setAccountSettings(accountSettings);
        dto.setApplicationSettings(new ApplicationSettings());
        dto.setSickNoteSettings(new SickNoteSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("accountSettings.defaultVacationDays", "error.entry.mandatory");
    }

    @Test
    void ensureDefaultVacationDaysAccountSettingsCanNotBeNegative() {

        final AccountSettings accountSettings = new AccountSettings();
        accountSettings.setDefaultVacationDays(-1);

        final SettingsAbsencesDto dto = new SettingsAbsencesDto();
        dto.setAccountSettings(accountSettings);
        dto.setApplicationSettings(new ApplicationSettings());
        dto.setSickNoteSettings(new SickNoteSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("accountSettings.defaultVacationDays", "error.entry.invalid");
    }

    @Test
    void ensureDefaultVacationDaysSmallerThanAYearWithLeapYearForExample() {

        final AccountSettings accountSettings = new AccountSettings();
        accountSettings.setDefaultVacationDays(367);

        final SettingsAbsencesDto dto = new SettingsAbsencesDto();
        dto.setAccountSettings(accountSettings);
        dto.setApplicationSettings(new ApplicationSettings());
        dto.setSickNoteSettings(new SickNoteSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("accountSettings.defaultVacationDays", "error.entry.invalid");
    }

    @Test
    void ensureDefaultVacationDaysSmallerMaxDays() {

        final AccountSettings accountSettings = new AccountSettings();
        accountSettings.setMaximumAnnualVacationDays(19);
        accountSettings.setDefaultVacationDays(20);

        final SettingsAbsencesDto dto = new SettingsAbsencesDto();
        dto.setAccountSettings(accountSettings);
        dto.setApplicationSettings(new ApplicationSettings());
        dto.setSickNoteSettings(new SickNoteSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("accountSettings.defaultVacationDays", "settings.account.error.defaultMustBeSmallerOrEqualThanMax");
    }

    // SickNote settings ------------------------------------------------------------------------------------------------
    @Test
    void ensureSickNoteSettingsCanNotBeNull() {

        final SickNoteSettings sickNoteSettings = new SickNoteSettings();
        sickNoteSettings.setMaximumSickPayDays(null);
        sickNoteSettings.setDaysBeforeEndOfSickPayNotification(null);

        final SettingsAbsencesDto dto = new SettingsAbsencesDto();
        dto.setSickNoteSettings(sickNoteSettings);
        dto.setApplicationSettings(new ApplicationSettings());
        dto.setAccountSettings(new AccountSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("sickNoteSettings.maximumSickPayDays", "error.entry.mandatory");
        verify(mockError).rejectValue("sickNoteSettings.daysBeforeEndOfSickPayNotification", "error.entry.mandatory");
    }

    @Test
    void ensureSickNoteSettingsCanNotBeSmallerThanLegalMinimum() {

        final SickNoteSettings sickNoteSettings = new SickNoteSettings();
        sickNoteSettings.setMaximumSickPayDays(41);

        final SettingsAbsencesDto dto = new SettingsAbsencesDto();
        dto.setSickNoteSettings(sickNoteSettings);
        dto.setApplicationSettings(new ApplicationSettings());
        dto.setAccountSettings(new AccountSettings());

        Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("sickNoteSettings.maximumSickPayDays", "sicknote.error.illegalMaximumSickPayDays");
    }

    @Test
    void ensureDaysBeforeEndOfSickPayNotificationCanNotBeNegative() {

        final SickNoteSettings sickNoteSettings = new SickNoteSettings();
        sickNoteSettings.setDaysBeforeEndOfSickPayNotification(-1);

        final SettingsAbsencesDto dto = new SettingsAbsencesDto();
        dto.setSickNoteSettings(sickNoteSettings);
        dto.setApplicationSettings(new ApplicationSettings());
        dto.setAccountSettings(new AccountSettings());

        Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("sickNoteSettings.daysBeforeEndOfSickPayNotification", "error.entry.invalid");
    }

    @Test
    void ensureThatDaysBeforeEndOfSickPayNotificationIsSmallerThanMaximumSickPayDays() {

        final SickNoteSettings sickNoteSettings = new SickNoteSettings();
        sickNoteSettings.setDaysBeforeEndOfSickPayNotification(11);
        sickNoteSettings.setMaximumSickPayDays(10);

        final SettingsAbsencesDto dto = new SettingsAbsencesDto();
        dto.setSickNoteSettings(sickNoteSettings);
        dto.setApplicationSettings(new ApplicationSettings());
        dto.setAccountSettings(new AccountSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("sickNoteSettings.daysBeforeEndOfSickPayNotification", "settings.sickDays.daysBeforeEndOfSickPayNotification.error");
    }
}
