package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SettingsOvertimeValidatorTest {

    private SettingsOvertimeValidator sut;

    @BeforeEach
    void setUp() {
        sut = new SettingsOvertimeValidator();
    }

    @Test
    void ensureSettingsClassIsSupported() {
        assertThat(sut.supports(SettingsOvertimeDto.class)).isTrue();
    }

    @Test
    void ensureOtherClassThanSettingsIsNotSupported() {
        assertThat(sut.supports(Object.class)).isFalse();
    }

    @Test
    void ensureOvertimeSettingsAreMandatoryIfOvertimeManagementIsActive() {

        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(true);
        overtimeSettings.setMaximumOvertime(null);
        overtimeSettings.setMinimumOvertime(null);

        final SettingsOvertimeDto dto = new SettingsOvertimeDto();
        dto.setOvertimeSettings(overtimeSettings);

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("overtimeSettings.maximumOvertime", "error.entry.mandatory");
        verify(mockError).rejectValue("overtimeSettings.minimumOvertime", "error.entry.mandatory");
    }

    @Test
    void ensureOvertimeSettingsAreNotMandatoryIfOvertimeManagementIsInactive() {

        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(false);
        overtimeSettings.setMaximumOvertime(null);
        overtimeSettings.setMinimumOvertime(null);

        final SettingsOvertimeDto dto = new SettingsOvertimeDto();
        dto.setOvertimeSettings(overtimeSettings);

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError, never()).rejectValue(anyString(), anyString());
    }

    @Test
    void ensureMaximumOvertimeCanNotBeNegative() {

        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(true);
        overtimeSettings.setMaximumOvertime(-1);

        final SettingsOvertimeDto dto = new SettingsOvertimeDto();
        dto.setOvertimeSettings(overtimeSettings);

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("overtimeSettings.maximumOvertime", "error.entry.invalid");
    }

    @Test
    void ensureMinimumOvertimeCanNotBeNegative() {

        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(true);
        overtimeSettings.setMinimumOvertime(-1);

        final SettingsOvertimeDto dto = new SettingsOvertimeDto();
        dto.setOvertimeSettings(overtimeSettings);

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("overtimeSettings.minimumOvertime", "error.entry.invalid");
    }
}
