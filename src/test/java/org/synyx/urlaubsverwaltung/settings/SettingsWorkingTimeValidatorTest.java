package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

        verifyNoInteractions(mockError);
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

    // Time settings -----------------------------------------------------------------------------------------------
    @Test
    void ensureCalendarSettingsAreMandatory() {

        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setWorkDayBeginHour(null);
        timeSettings.setWorkDayEndHour(null);

        final SettingsWorkingTimeDto dto = new SettingsWorkingTimeDto();
        dto.setTimeSettings(timeSettings);
        dto.setWorkingTimeSettings(new WorkingTimeSettings());
        dto.setOvertimeSettings(new OvertimeSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("timeSettings.workDayBeginHour", "error.entry.mandatory");
        verify(mockError).rejectValue("timeSettings.workDayEndHour", "error.entry.mandatory");
    }

    private static Stream<Arguments> invalidWorkingHours() {
        return Stream.of(
            Arguments.of(8, 8), // same hours
            Arguments.of(17, 8), // begin is later than end
            Arguments.of(-1, -2), // negative values
            Arguments.of(25, 42) // more than 24 hours
        );
    }

    @ParameterizedTest
    @MethodSource("invalidWorkingHours")
    void ensureCalendarSettingsAreInvalid(int beginHour, int endHour) {

        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setWorkDayBeginHour(beginHour);
        timeSettings.setWorkDayEndHour(endHour);

        final SettingsWorkingTimeDto dto = new SettingsWorkingTimeDto();
        dto.setTimeSettings(timeSettings);
        dto.setWorkingTimeSettings(new WorkingTimeSettings());
        dto.setOvertimeSettings(new OvertimeSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("timeSettings.workDayBeginHour", "error.entry.invalid");
        verify(mockError).rejectValue("timeSettings.workDayEndHour", "error.entry.invalid");
    }

    @Test
    void ensureCalendarSettingsAreValidIfValidWorkDayBeginAndEndHours() {

        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setWorkDayBeginHour(10);
        timeSettings.setWorkDayEndHour(18);

        final SettingsWorkingTimeDto dto = new SettingsWorkingTimeDto();
        dto.setTimeSettings(timeSettings);
        dto.setWorkingTimeSettings(new WorkingTimeSettings());
        dto.setOvertimeSettings(new OvertimeSettings());

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verifyNoInteractions(mockError);
    }
}
