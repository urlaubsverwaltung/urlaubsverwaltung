package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SettingsPublicHolidayValidatorTest {

    private SettingsPublicHolidayValidator sut;

    @BeforeEach
    void setUp() {
        sut = new SettingsPublicHolidayValidator();
    }

    @Test
    void ensureSettingsClassIsSupported() {
        assertThat(sut.supports(SettingsPublicHolidayDto.class)).isTrue();
    }

    @Test
    void ensureOtherClassThanSettingsIsNotSupported() {
        assertThat(sut.supports(Object.class)).isFalse();
    }

    @Test
    void ensureWorkingTimeSettingsCanNotBeNull() {

        final PublicHolidaysSettings publicHolidaysSettings = new PublicHolidaysSettings();
        publicHolidaysSettings.setFederalState(null);
        publicHolidaysSettings.setWorkingDurationForChristmasEve(null);
        publicHolidaysSettings.setWorkingDurationForNewYearsEve(null);

        final SettingsPublicHolidayDto dto = new SettingsPublicHolidayDto();
        dto.setPublicHolidaysSettings(publicHolidaysSettings);

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("publicHolidaysSettings.federalState", "error.entry.mandatory");
        verify(mockError).rejectValue("publicHolidaysSettings.workingDurationForChristmasEve", "error.entry.mandatory");
        verify(mockError).rejectValue("publicHolidaysSettings.workingDurationForNewYearsEve", "error.entry.mandatory");
    }
}
