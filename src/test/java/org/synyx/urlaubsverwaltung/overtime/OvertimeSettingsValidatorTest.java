package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class OvertimeSettingsValidatorTest {

    @Mock
    private Errors errors;

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = -1)
    void ensureMinimumOvertimeReductionValidationFails(Integer givenValue) {

        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(true);
        overtimeSettings.setMinimumOvertimeReduction(givenValue);

        OvertimeSettingsValidator.validateOvertimeSettings(overtimeSettings, errors);

        verify(errors).rejectValue("overtimeSettings.minimumOvertimeReduction", "settings.overtime.minimumOvertimeReduction.error");
        verifyNoMoreInteractions(errors);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void ensureMinimumOvertimeReductionValidationSucceeds(int givenMinimumReduction) {

        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(true);
        overtimeSettings.setMinimumOvertimeReduction(givenMinimumReduction);

        OvertimeSettingsValidator.validateOvertimeSettings(overtimeSettings, errors);

        verifyNoInteractions(errors);
    }
}
