package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.validation.Errors;
import org.springframework.validation.SimpleErrors;
import org.synyx.urlaubsverwaltung.calendar.TimeSettings;
import org.synyx.urlaubsverwaltung.calendar.TimeSettingsValidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

class SettingsCalendarValidatorTest {

    private SettingsCalendarValidator sut;

    @BeforeEach
    void setUp() {
        sut = new SettingsCalendarValidator();
    }

    @Test
    void ensureTimeSettingsValidated() {

        final TimeSettings timeSettings = new TimeSettings();
        final SettingsCalendarDto settingsCalendarDto = new SettingsCalendarDto();
        settingsCalendarDto.setTimeSettings(timeSettings);

        final SimpleErrors errors = new SimpleErrors(settingsCalendarDto);

        try (final MockedStatic<TimeSettingsValidator> timeSettingsValidatorMock = mockStatic(TimeSettingsValidator.class)) {

            sut.validate(settingsCalendarDto, errors);

            timeSettingsValidatorMock.verify(() -> {

                final ArgumentCaptor<TimeSettings> timeSettingsCaptor = ArgumentCaptor.forClass(TimeSettings.class);
                final ArgumentCaptor<Errors> errorsCaptor = ArgumentCaptor.forClass(Errors.class);

                TimeSettingsValidator.validateTimeSettings(timeSettingsCaptor.capture(), errorsCaptor.capture());

                assertThat(timeSettingsCaptor.getValue()).isSameAs(timeSettings);
                assertThat(errorsCaptor.getValue()).isSameAs(errors);
            });
        }
    }
}
