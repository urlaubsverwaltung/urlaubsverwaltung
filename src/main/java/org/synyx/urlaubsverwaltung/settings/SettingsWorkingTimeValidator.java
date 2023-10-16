package org.synyx.urlaubsverwaltung.settings;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.synyx.urlaubsverwaltung.absence.TimeSettingsValidator.validateTimeSettings;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeSettingsValidator.validateOvertimeSettings;
import static org.synyx.urlaubsverwaltung.workingtime.WorkTimeSettingsValidator.validateWorkingTimeSettings;

@Component
public class SettingsWorkingTimeValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(SettingsWorkingTimeDto.class);
    }

    @Override
    public void validate(Object o, @NonNull Errors errors) {
        final SettingsWorkingTimeDto settings = (SettingsWorkingTimeDto) o;
        validateWorkingTimeSettings(settings.getWorkingTimeSettings(), errors);
        validateOvertimeSettings(settings.getOvertimeSettings(), errors);
        validateTimeSettings(settings.getTimeSettings(), errors);
    }
}
