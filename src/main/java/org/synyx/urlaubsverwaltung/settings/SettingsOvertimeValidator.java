package org.synyx.urlaubsverwaltung.settings;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.synyx.urlaubsverwaltung.overtime.OvertimeSettingsValidator.validateOvertimeSettings;

@Component
public class SettingsOvertimeValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(SettingsOvertimeDto.class);
    }

    @Override
    public void validate(Object o, @NonNull Errors errors) {
        final SettingsOvertimeDto settings = (SettingsOvertimeDto) o;
        validateOvertimeSettings(settings.getOvertimeSettings(), errors);
    }
}
