package org.synyx.urlaubsverwaltung.absence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.settings.Settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.absence.TimeSettingsValidator.validateTimeSettings;

class TimeSettingsValidatorTest {

    @Test
    void ensureThatStartTimeIsBeforeEndTime() {

        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setWorkDayBeginHour(9);
        timeSettings.setWorkDayBeginMinute(10);
        timeSettings.setWorkDayEndHour(9);
        timeSettings.setWorkDayEndMinute(11);

        final Settings settings = new Settings();
        settings.setTimeSettings(timeSettings);

        final Errors errors = new BeanPropertyBindingResult(settings, "settings");

        validateTimeSettings(timeSettings, errors);

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void ensureToHaveAnErrorIfStartTimeAndEndTimeAreTheSame() {

        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setWorkDayBeginHour(9);
        timeSettings.setWorkDayBeginMinute(10);
        timeSettings.setWorkDayEndHour(9);
        timeSettings.setWorkDayEndMinute(10);

        final Settings settings = new Settings();
        settings.setTimeSettings(timeSettings);

        final Errors errors = new BeanPropertyBindingResult(settings, "settings");

        validateTimeSettings(timeSettings, errors);

        assertThat(errors.hasErrors()).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"-1, -1", "0, -1", "24, 60"})
    void ensureStartTimeCanNotBeInvalid(final Integer hour, final Integer minute) {

        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setWorkDayBeginHour(hour);
        timeSettings.setWorkDayBeginMinute(minute);

        final Settings settings = new Settings();
        settings.setTimeSettings(timeSettings);

        final Errors errors = new BeanPropertyBindingResult(settings, "settings");

        validateTimeSettings(timeSettings, errors);

        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldErrors("timeSettings.workDayBeginHour")).extracting(DefaultMessageSourceResolvable::getCode).contains("error.entry.invalid");
        assertThat(errors.getFieldErrors("timeSettings.workDayBeginMinute")).extracting(DefaultMessageSourceResolvable::getCode).contains("error.entry.invalid");
    }

    @Test
    void ensureStartTimeCanNotBeNull() {

        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setWorkDayBeginHour(null);
        timeSettings.setWorkDayBeginMinute(null);

        final Settings settings = new Settings();
        settings.setTimeSettings(timeSettings);

        final Errors errors = new BeanPropertyBindingResult(settings, "settings");

        validateTimeSettings(timeSettings, errors);

        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldErrors("timeSettings.workDayBeginHour")).extracting(DefaultMessageSourceResolvable::getCode).contains("error.entry.mandatory");
        assertThat(errors.getFieldErrors("timeSettings.workDayBeginMinute")).extracting(DefaultMessageSourceResolvable::getCode).contains("error.entry.mandatory");
    }

    @ParameterizedTest
    @CsvSource({"-1, -1", "0, -1", "24, 60"})
    void ensureEndTimeCanNotBeInvalid(final Integer hour, final Integer minute) {

        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setWorkDayEndHour(hour);
        timeSettings.setWorkDayEndMinute(minute);

        final Settings settings = new Settings();
        settings.setTimeSettings(timeSettings);

        final Errors errors = new BeanPropertyBindingResult(settings, "settings");

        validateTimeSettings(timeSettings, errors);

        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldErrors("timeSettings.workDayEndHour")).extracting(DefaultMessageSourceResolvable::getCode).contains("error.entry.invalid");
        assertThat(errors.getFieldErrors("timeSettings.workDayEndMinute")).extracting(DefaultMessageSourceResolvable::getCode).contains("error.entry.invalid");
    }

    @Test
    void ensureEndTimeCanNotBeNull() {

        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setWorkDayEndHour(null);
        timeSettings.setWorkDayEndMinute(null);

        final Settings settings = new Settings();
        settings.setTimeSettings(timeSettings);

        final Errors errors = new BeanPropertyBindingResult(settings, "settings");

        validateTimeSettings(timeSettings, errors);

        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldErrors("timeSettings.workDayEndHour")).extracting(DefaultMessageSourceResolvable::getCode).contains("error.entry.mandatory");
        assertThat(errors.getFieldErrors("timeSettings.workDayEndMinute")).extracting(DefaultMessageSourceResolvable::getCode).contains("error.entry.mandatory");
    }
}
