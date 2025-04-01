package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.account.AccountSettings;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SettingsAccountValidatorTest {

    private SettingsAccountValidator sut;

    @BeforeEach
    void setUp() {
        sut = new SettingsAccountValidator();
    }

    @Test
    void ensureSettingsClassIsSupported() {
        assertThat(sut.supports(SettingsAccountDto.class)).isTrue();
    }

    @Test
    void ensureOtherClassThanSettingsIsNotSupported() {
        assertThat(sut.supports(Object.class)).isFalse();
    }

    // Account settings ------------------------------------------------------------------------------------------------
    @Test
    void ensureMaximumAnnualVacationDaysAccountSettingsCanNotBeNull() {

        final AccountSettings accountSettings = new AccountSettings();
        accountSettings.setMaximumAnnualVacationDays(null);

        final SettingsAccountDto dto = new SettingsAccountDto();
        dto.setAccountSettings(accountSettings);
        dto.setWorkingTimeSettings(new WorkingTimeSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("accountSettings.maximumAnnualVacationDays", "error.entry.mandatory");
    }

    @Test
    void ensureMaximumAnnualVacationDaysAccountSettingsCanNotBeNegative() {

        final AccountSettings accountSettings = new AccountSettings();
        accountSettings.setMaximumAnnualVacationDays(-1);

        final SettingsAccountDto dto = new SettingsAccountDto();
        dto.setAccountSettings(accountSettings);
        dto.setWorkingTimeSettings(new WorkingTimeSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("accountSettings.maximumAnnualVacationDays", "error.entry.invalid");
    }

    @Test
    void ensureDefaultVacationDaysAccountSettingsCanNotBeNull() {

        final AccountSettings accountSettings = new AccountSettings();
        accountSettings.setDefaultVacationDays(null);

        final SettingsAccountDto dto = new SettingsAccountDto();
        dto.setAccountSettings(accountSettings);
        dto.setWorkingTimeSettings(new WorkingTimeSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("accountSettings.defaultVacationDays", "error.entry.mandatory");
    }

    @Test
    void ensureDefaultVacationDaysAccountSettingsCanNotBeNegative() {

        final AccountSettings accountSettings = new AccountSettings();
        accountSettings.setDefaultVacationDays(-1);

        final SettingsAccountDto dto = new SettingsAccountDto();
        dto.setAccountSettings(accountSettings);
        dto.setWorkingTimeSettings(new WorkingTimeSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("accountSettings.defaultVacationDays", "error.entry.invalid");
    }

    @Test
    void ensureDefaultVacationDaysSmallerThanAYearWithLeapYearForExample() {

        final AccountSettings accountSettings = new AccountSettings();
        accountSettings.setDefaultVacationDays(367);

        final SettingsAccountDto dto = new SettingsAccountDto();
        dto.setAccountSettings(accountSettings);
        dto.setWorkingTimeSettings(new WorkingTimeSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("accountSettings.defaultVacationDays", "error.entry.invalid");
    }

    @Test
    void ensureDefaultVacationDaysSmallerMaxDays() {

        final AccountSettings accountSettings = new AccountSettings();
        accountSettings.setMaximumAnnualVacationDays(19);
        accountSettings.setDefaultVacationDays(20);

        final SettingsAccountDto dto = new SettingsAccountDto();
        dto.setAccountSettings(accountSettings);
        dto.setWorkingTimeSettings(new WorkingTimeSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("accountSettings.defaultVacationDays", "settings.account.error.defaultMustBeSmallerOrEqualThanMax");
    }
}
