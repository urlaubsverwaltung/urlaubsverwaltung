package org.synyx.urlaubsverwaltung.publicholiday;

import org.junit.jupiter.api.Test;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class PublicHolidaysSettingsValidatorTest {

    @Test
    void ensureInvalidWhenFederStateIsNull() {

        final PublicHolidaysSettings settings = new PublicHolidaysSettings();
        settings.setFederalState(null);

        final Errors errors = mock(Errors.class);

        PublicHolidaysSettingsValidator.validatePublicHolidaysSettings(settings, errors);

        verify(errors).rejectValue("publicHolidaysSettings.federalState","error.entry.mandatory");
    }

    @Test
    void ensureInvalidWhenWorkingDurationForChristmasEveIsNull() {

        final PublicHolidaysSettings settings = new PublicHolidaysSettings();
        settings.setWorkingDurationForChristmasEve(null);

        final Errors errors = mock(Errors.class);

        PublicHolidaysSettingsValidator.validatePublicHolidaysSettings(settings, errors);

        verify(errors).rejectValue("publicHolidaysSettings.workingDurationForChristmasEve","error.entry.mandatory");
    }

    @Test
    void ensureInvalidWhenWorkingDurationForNewYearsEveIsNull() {

        final PublicHolidaysSettings settings = new PublicHolidaysSettings();
        settings.setWorkingDurationForNewYearsEve(null);

        final Errors errors = mock(Errors.class);

        PublicHolidaysSettingsValidator.validatePublicHolidaysSettings(settings, errors);

        verify(errors).rejectValue("publicHolidaysSettings.workingDurationForNewYearsEve","error.entry.mandatory");
    }

    @Test
    void ensureValid() {

        final PublicHolidaysSettings settings = new PublicHolidaysSettings();
        settings.setFederalState(FederalState.GERMANY_BADEN_WUERTTEMBERG);
        settings.setWorkingDurationForChristmasEve(DayLength.ZERO);
        settings.setWorkingDurationForNewYearsEve(DayLength.ZERO);

        final Errors errors = mock(Errors.class);

        PublicHolidaysSettingsValidator.validatePublicHolidaysSettings(settings, errors);

        verifyNoInteractions(errors);
    }
}
