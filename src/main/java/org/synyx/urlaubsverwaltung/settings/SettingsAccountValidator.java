package org.synyx.urlaubsverwaltung.settings;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.synyx.urlaubsverwaltung.account.AccountSettingsValidator.validateAccountSettings;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettingsValidator.validateWorkingTimeSettings;

@Component
public class SettingsAccountValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(SettingsAccountDto.class);
    }

    @Override
    public void validate(Object o, @NonNull Errors errors) {
        final SettingsAccountDto settings = (SettingsAccountDto) o;
        validateWorkingTimeSettings(settings.getWorkingTimeSettings(), errors);
        validateAccountSettings(settings.getAccountSettings(), errors);
    }
}
