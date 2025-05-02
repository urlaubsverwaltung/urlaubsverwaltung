package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.period.DayLength;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class WorkingTimeSettingsValidatorTest {

    @Test
    void ensureWorkingDaysInvalidWithoutAnyWorkingDay() {

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setMonday(DayLength.ZERO);
        workingTimeSettings.setTuesday(DayLength.ZERO);
        workingTimeSettings.setWednesday(DayLength.ZERO);
        workingTimeSettings.setThursday(DayLength.ZERO);
        workingTimeSettings.setFriday(DayLength.ZERO);
        workingTimeSettings.setSaturday(DayLength.ZERO);
        workingTimeSettings.setSunday(DayLength.ZERO);

        final Errors errors = Mockito.mock(Errors.class);

        WorkingTimeSettingsValidator.validateWorkingTimeSettings(workingTimeSettings, errors);

        verify(errors).rejectValue("workingTimeSettings.workingDays","settings.workingTime.error.mandatory");
    }

    @Test
    void ensureWorkingDaysValidWithAtLeastOneWorkingDay() {

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setMonday(DayLength.FULL);
        workingTimeSettings.setTuesday(DayLength.ZERO);
        workingTimeSettings.setWednesday(DayLength.ZERO);
        workingTimeSettings.setThursday(DayLength.ZERO);
        workingTimeSettings.setFriday(DayLength.ZERO);
        workingTimeSettings.setSaturday(DayLength.ZERO);
        workingTimeSettings.setSunday(DayLength.ZERO);

        final Errors errors = Mockito.mock(Errors.class);

        WorkingTimeSettingsValidator.validateWorkingTimeSettings(workingTimeSettings, errors);

        verifyNoInteractions(errors);
    }
}
