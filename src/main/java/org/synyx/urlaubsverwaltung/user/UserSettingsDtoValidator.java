package org.synyx.urlaubsverwaltung.user;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Locale;

import static java.util.Arrays.stream;

@Component
class UserSettingsDtoValidator implements Validator {

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return UserSettingsDto.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        final UserSettingsDto userSettingsDto = (UserSettingsDto) target;

        final String selectedTheme = userSettingsDto.getSelectedTheme();
        if (availableThemesDoesNotContain(selectedTheme)) {
            errors.reject("Theme is not available");
        }

        final Locale locale = userSettingsDto.getLocale();
        if (supportedLocaleDoesNotContain(locale)) {
            errors.reject("Locale is not available");
        }
    }

    private static boolean supportedLocaleDoesNotContain(Locale locale) {
        return stream(SupportedLocale.values()).noneMatch(supportedLocale -> supportedLocale.getLocale().equals(locale));
    }

    private static boolean availableThemesDoesNotContain(String theme) {
        return stream(Theme.values()).noneMatch(availableTheme -> availableTheme.name().equals(theme));
    }
}
