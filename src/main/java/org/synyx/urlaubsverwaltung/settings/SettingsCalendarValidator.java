package org.synyx.urlaubsverwaltung.settings;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.synyx.urlaubsverwaltung.calendar.TimeSettingsValidator.validateTimeSettings;

@Component
public class SettingsCalendarValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(SettingsCalendarDto.class);
    }

    @Override
    public void validate(Object o, @NonNull Errors errors) {
        final SettingsCalendarDto settings = (SettingsCalendarDto) o;
        validateTimeSettings(settings.getTimeSettings(), errors);
    }
}
