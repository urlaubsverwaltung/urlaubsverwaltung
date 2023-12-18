package org.synyx.urlaubsverwaltung.calendarintegration;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
class SettingsCalendarSyncValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_LENGTH = "error.entry.tooManyChars";
    private static final int MAX_CHARS = 255;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(SettingsCalendarSyncDto.class);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        final SettingsCalendarSyncDto settings = (SettingsCalendarSyncDto) target;
        validateCalendarSettings(settings.getCalendarSettings(), errors);
    }


    private void validateCalendarSettings(CalendarSettings calendarSettings, Errors errors) {

        if (calendarSettings.getProvider() == null) {
            return;
        }

        if (calendarSettings.getProvider().equals(GoogleCalendarSyncProvider.class.getSimpleName())) {
            validateGoogleCalendarSettings(calendarSettings.getGoogleCalendarSettings(), errors);
        }
    }

    private void validateGoogleCalendarSettings(GoogleCalendarSettings googleCalendarSettings, Errors errors) {

        final String calendarId = googleCalendarSettings.getCalendarId();
        final String clientId = googleCalendarSettings.getClientId();
        final String clientSecret = googleCalendarSettings.getClientSecret();

        validateMandatoryTextField(calendarId, "calendarSettings.googleCalendarSettings.calendarId", errors);
        validateMandatoryTextField(clientId, "calendarSettings.googleCalendarSettings.clientId", errors);
        validateMandatoryTextField(clientSecret, "calendarSettings.googleCalendarSettings.clientSecret", errors);
    }

    private void validateMandatoryTextField(String input, String attributeName, Errors errors) {
        if (!StringUtils.hasText(input)) {
            errors.rejectValue(attributeName, ERROR_MANDATORY_FIELD);
        } else {
            if (!validStringLength(input)) {
                errors.rejectValue(attributeName, ERROR_LENGTH);
            }
        }
    }

    private boolean validStringLength(String string) {
        return string.length() <= MAX_CHARS;
    }
}
