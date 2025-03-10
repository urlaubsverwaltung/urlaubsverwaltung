package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;

class SettingsWorkingTimeValidatorTest {

    private SettingsWorkingTimeValidator sut;

    @BeforeEach
    void setUp() {
        sut = new SettingsWorkingTimeValidator();
    }

    @Test
    void ensureSettingsClassIsSupported() {
        assertThat(sut.supports(SettingsWorkingTimeDto.class)).isTrue();
    }

    @Test
    void ensureOtherClassThanSettingsIsNotSupported() {
        assertThat(sut.supports(Object.class)).isFalse();
    }

    // Public holidays --------------------------------------------------------------------------
    @Test
    void ensureWorkingTimeSettingsCanNotBeNull() {

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setFederalState(null);
        workingTimeSettings.setWorkingDurationForChristmasEve(null);
        workingTimeSettings.setWorkingDurationForNewYearsEve(null);
        workingTimeSettings.setMonday(ZERO);
        workingTimeSettings.setTuesday(ZERO);
        workingTimeSettings.setWednesday(ZERO);
        workingTimeSettings.setThursday(ZERO);
        workingTimeSettings.setFriday(ZERO);
        workingTimeSettings.setSaturday(ZERO);
        workingTimeSettings.setSunday(ZERO);

        final SettingsWorkingTimeDto dto = new SettingsWorkingTimeDto();
        dto.setWorkingTimeSettings(workingTimeSettings);
        dto.setOvertimeSettings(new OvertimeSettings());
        dto.setTimeSettings(new TimeSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("workingTimeSettings.federalState", "error.entry.mandatory");
        verify(mockError).rejectValue("workingTimeSettings.workingDurationForChristmasEve", "error.entry.mandatory");
        verify(mockError).rejectValue("workingTimeSettings.workingDurationForNewYearsEve", "error.entry.mandatory");
        verify(mockError).rejectValue("workingTimeSettings.workingDays", "settings.workingTime.error.mandatory");
    }

    // Overtime settings ------------------------------------------------------------------------
    @Test
    void ensureOvertimeSettingsAreMandatoryIfOvertimeManagementIsActive() {

        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(true);
        overtimeSettings.setMaximumOvertime(null);
        overtimeSettings.setMinimumOvertime(null);

        final SettingsWorkingTimeDto dto = new SettingsWorkingTimeDto();
        dto.setOvertimeSettings(overtimeSettings);
        dto.setWorkingTimeSettings(new WorkingTimeSettings());
        dto.setTimeSettings(new TimeSettings());

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

        final SettingsWorkingTimeDto dto = new SettingsWorkingTimeDto();
        dto.setOvertimeSettings(overtimeSettings);
        dto.setWorkingTimeSettings(new WorkingTimeSettings());
        dto.setTimeSettings(new TimeSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError, never()).rejectValue(anyString(), anyString());
    }

    @Test
    void ensureMaximumOvertimeCanNotBeNegative() {

        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(true);
        overtimeSettings.setMaximumOvertime(-1);

        final SettingsWorkingTimeDto dto = new SettingsWorkingTimeDto();
        dto.setOvertimeSettings(overtimeSettings);
        dto.setWorkingTimeSettings(new WorkingTimeSettings());
        dto.setTimeSettings(new TimeSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("overtimeSettings.maximumOvertime", "error.entry.invalid");
    }

    @Test
    void ensureMinimumOvertimeCanNotBeNegative() {

        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(true);
        overtimeSettings.setMinimumOvertime(-1);

        final SettingsWorkingTimeDto dto = new SettingsWorkingTimeDto();
        dto.setOvertimeSettings(overtimeSettings);
        dto.setWorkingTimeSettings(new WorkingTimeSettings());
        dto.setTimeSettings(new TimeSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("overtimeSettings.minimumOvertime", "error.entry.invalid");
    }
}
