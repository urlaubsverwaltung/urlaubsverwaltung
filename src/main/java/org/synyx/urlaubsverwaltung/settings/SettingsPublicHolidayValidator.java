package org.synyx.urlaubsverwaltung.settings;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysSettingsValidator.validatePublicHolidaysSettings;


@Component
public class SettingsPublicHolidayValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(SettingsPublicHolidayDto.class);
    }

    @Override
    public void validate(Object o, @NonNull Errors errors) {
        final SettingsPublicHolidayDto settings = (SettingsPublicHolidayDto) o;
        validatePublicHolidaysSettings(settings.getPublicHolidaysSettings(), errors);

        if (settings.getWorkingDurationForChristmasEve() == null) {
            errors.rejectValue("workingDurationForChristmasEve", ERROR_MANDATORY_FIELD);
        }

        if (settings.getWorkingDurationForNewYearsEve() == null) {
            errors.rejectValue("workingDurationForNewYearsEve", ERROR_MANDATORY_FIELD);
        }
    }
}
