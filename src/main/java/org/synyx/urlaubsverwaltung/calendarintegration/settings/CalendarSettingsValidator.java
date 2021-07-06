package org.synyx.urlaubsverwaltung.calendarintegration.settings;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.exchange.ExchangeCalendarProvider;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.google.GoogleCalendarSyncProvider;
import org.synyx.urlaubsverwaltung.web.MailAddressValidationUtil;

class CalendarSettingsValidator  {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_EMAIL = "error.entry.mail";
    private static final String ERROR_LENGTH = "error.entry.tooManyChars";
    private static final int MAX_CHARS = 255;


    public static Errors validateCalendarSettings(CalendarSettingsDto calendarSettings, Errors errors) {

        if (calendarSettings.getProvider().equals(ExchangeCalendarProvider.class.getSimpleName())) {
            validateExchangeCalendarSettings(calendarSettings.getExchangeCalendarSettings(), errors);
        }
        if (calendarSettings.getProvider().equals(GoogleCalendarSyncProvider.class.getSimpleName())) {
            validateGoogleCalendarSettings(calendarSettings.getGoogleCalendarSettings(), errors);
        }
        return errors;
    }

    private static void validateExchangeCalendarSettings(ExchangeCalendarSettingsDto exchangeCalendarSettings, Errors errors) {
        validateExchangeEmail(exchangeCalendarSettings, errors);
        validateExchangePassword(exchangeCalendarSettings, errors);
        validateExchangeCalendarName(exchangeCalendarSettings, errors);
    }

    private static void validateGoogleCalendarSettings(GoogleCalendarSettingsDto googleCalendarSettings, Errors errors) {
        String calendarId = googleCalendarSettings.getCalendarId();
        String clientId = googleCalendarSettings.getClientId();
        String clientSecret = googleCalendarSettings.getClientSecret();

        validateMandatoryTextField(calendarId, "calendarSettings.googleCalendarSettings.calendarId", errors);
        validateMandatoryTextField(clientId, "calendarSettings.googleCalendarSettings.clientId", errors);
        validateMandatoryTextField(clientSecret, "calendarSettings.googleCalendarSettings.clientSecret", errors);
    }

    private static void validateExchangeEmail(ExchangeCalendarSettingsDto exchangeCalendarSettings, Errors errors) {

        String emailAttribute = "calendarSettings.exchangeCalendarSettings.email";
        String email = exchangeCalendarSettings.getEmail();

        validateMandatoryMailAddress(email, emailAttribute, errors);
    }

    private static void validateExchangePassword(ExchangeCalendarSettingsDto exchangeCalendarSettings, Errors errors) {

        String passwordAttribute = "calendarSettings.exchangeCalendarSettings.password";
        String password = exchangeCalendarSettings.getPassword();

        validateMandatoryTextField(password, passwordAttribute, errors);
    }

    private static void validateExchangeCalendarName(ExchangeCalendarSettingsDto exchangeCalendarSettings, Errors errors) {

        String calendarAttribute = "calendarSettings.exchangeCalendarSettings.calendar";
        String calendar = exchangeCalendarSettings.getPassword();

        validateMandatoryTextField(calendar, calendarAttribute, errors);
    }

    private static void validateMandatoryTextField(String input, String attributeName, Errors errors) {
        if (!StringUtils.hasText(input)) {
            errors.rejectValue(attributeName, ERROR_MANDATORY_FIELD);
        } else {
            if (!validStringLength(input)) {
                errors.rejectValue(attributeName, ERROR_LENGTH);
            }
        }
    }

    private static void validateMandatoryMailAddress(String mailAddress, String attributeName, Errors errors) {

        if (!StringUtils.hasText(mailAddress)) {
            errors.rejectValue(attributeName, ERROR_MANDATORY_FIELD);
        } else {
            if (!validStringLength(mailAddress)) {
                errors.rejectValue(attributeName, ERROR_LENGTH);
            }

            if (!MailAddressValidationUtil.hasValidFormat(mailAddress)) {
                errors.rejectValue(attributeName, ERROR_INVALID_EMAIL);
            }
        }
    }

    private static boolean validStringLength(String string) {
        return string.length() <= MAX_CHARS;
    }
}
