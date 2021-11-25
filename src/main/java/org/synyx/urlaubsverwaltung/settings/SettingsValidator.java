package org.synyx.urlaubsverwaltung.settings;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.synyx.urlaubsverwaltung.absence.TimeSettingsValidator.validateTimeSettings;
import static org.synyx.urlaubsverwaltung.account.AccountSettingsValidator.validateAccountSettings;
import static org.synyx.urlaubsverwaltung.application.settings.ApplicationSettingsValidator.validateApplicationSettings;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeSettingsValidator.validateOvertimeSettings;
import static org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettingsValidator.validateSickNoteSettings;
import static org.synyx.urlaubsverwaltung.workingtime.WorkTimeSettingsValidator.validateWorkingTimeSettings;

@Component
public class SettingsValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(Settings.class);
    }

    @Override
    public void validate(Object o, @NonNull Errors errors) {

        Assert.isTrue(supports(o.getClass()), "The given object must be an instance of Settings");

        final Settings settings = (Settings) o;
        validateWorkingTimeSettings(settings.getWorkingTimeSettings(), errors);
        validateOvertimeSettings(settings.getOvertimeSettings(), errors);
        validateApplicationSettings(settings.getApplicationSettings(), errors);
        validateAccountSettings(settings.getAccountSettings(), errors);
        validateSickNoteSettings(settings.getSickNoteSettings(), errors);
        validateTimeSettings(settings.getTimeSettings(), errors);
    }
}
