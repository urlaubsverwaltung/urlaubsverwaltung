package org.synyx.urlaubsverwaltung.settings;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.synyx.urlaubsverwaltung.account.AccountSettingsValidator.validateAccountSettings;
import static org.synyx.urlaubsverwaltung.application.settings.ApplicationSettingsValidator.validateApplicationSettings;
import static org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettingsValidator.validateSickNoteSettings;

@Component
public class SettingsAbsencesValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(SettingsAbsencesDto.class);
    }

    @Override
    public void validate(Object o, @NonNull Errors errors) {
        final SettingsAbsencesDto settings = (SettingsAbsencesDto) o;
        validateApplicationSettings(settings.getApplicationSettings(), errors);
        validateAccountSettings(settings.getAccountSettings(), errors);
        validateSickNoteSettings(settings.getSickNoteSettings(), errors);
    }
}
