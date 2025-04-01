package org.synyx.urlaubsverwaltung.settings;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.synyx.urlaubsverwaltung.workingtime.WorkTimeSettingsValidator.validateWorkingTimeSettings;


@Component
public class SettingsPublicHolidayValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(SettingsPublicHolidayDto.class);
    }

    @Override
    public void validate(Object o, @NonNull Errors errors) {
        final SettingsPublicHolidayDto settings = (SettingsPublicHolidayDto) o;
        validateWorkingTimeSettings(settings.getWorkingTimeSettings(), errors);
    }
}
