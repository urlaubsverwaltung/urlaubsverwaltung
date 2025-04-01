package org.synyx.urlaubsverwaltung.settings;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettingsValidator.validateWorkingTimeSettings;

@Component
public class SettingsOnboardingValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(SettingsOnboardingDto.class);
    }

    @Override
    public void validate(Object o, @NonNull Errors errors) {
        final SettingsOnboardingDto settings = (SettingsOnboardingDto) o;
        validateWorkingTimeSettings(settings.getWorkingTimeSettings(), errors);
    }
}
