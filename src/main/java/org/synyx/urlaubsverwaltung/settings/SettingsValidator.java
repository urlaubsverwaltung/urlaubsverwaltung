package org.synyx.urlaubsverwaltung.settings;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSettings;
import org.synyx.urlaubsverwaltung.calendarintegration.ExchangeCalendarSettings;
import org.synyx.urlaubsverwaltung.calendarintegration.GoogleCalendarSettings;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.exchange.ExchangeCalendarProvider;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.google.GoogleCalendarSyncProvider;
import org.synyx.urlaubsverwaltung.web.MailAddressValidationUtil;

import static org.synyx.urlaubsverwaltung.absence.TimeSettingsValidator.validateTimeSettings;
import static org.synyx.urlaubsverwaltung.account.AccountSettingsValidator.validateAccountSettings;
import static org.synyx.urlaubsverwaltung.application.settings.ApplicationSettingsValidator.validateApplicationSettings;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeSettingsValidator.validateOvertimeSettings;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteSettingsValidator.validateSickNoteSettings;
import static org.synyx.urlaubsverwaltung.workingtime.WorkTimeSettingsValidator.validateWorkingTimeSettings;

@Component
public class SettingsValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_EMAIL = "error.entry.mail";
    private static final String ERROR_LENGTH = "error.entry.tooManyChars";
    private static final int MAX_CHARS = 255;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(Settings.class);
    }

    @Override
    public void validate(Object o, Errors errors) {

        Assert.isTrue(supports(o.getClass()), "The given object must be an instance of Settings");

        final Settings settings = (Settings) o;
        validateWorkingTimeSettings(settings.getWorkingTimeSettings(), errors);
        validateOvertimeSettings(settings.getOvertimeSettings(), errors);
        validateApplicationSettings(settings.getApplicationSettings(), errors);
        validateAccountSettings(settings.getAccountSettings(), errors);
        validateSickNoteSettings(settings.getSickNoteSettings(), errors);
        validateCalendarSettings(settings.getCalendarSettings(), errors);
        validateTimeSettings(settings.getTimeSettings(), errors);
    }


    private void validateCalendarSettings(CalendarSettings calendarSettings, Errors errors) {

        if (calendarSettings.getProvider().equals(ExchangeCalendarProvider.class.getSimpleName())) {
            validateExchangeCalendarSettings(calendarSettings.getExchangeCalendarSettings(), errors);
        }
        if (calendarSettings.getProvider().equals(GoogleCalendarSyncProvider.class.getSimpleName())) {
            validateGoogleCalendarSettings(calendarSettings.getGoogleCalendarSettings(), errors);
        }
    }

    private void validateExchangeCalendarSettings(ExchangeCalendarSettings exchangeCalendarSettings, Errors errors) {
        validateExchangeEmail(exchangeCalendarSettings, errors);
        validateExchangePassword(exchangeCalendarSettings, errors);
        validateExchangeCalendarName(exchangeCalendarSettings, errors);
    }

    private void validateGoogleCalendarSettings(GoogleCalendarSettings googleCalendarSettings, Errors errors) {
        String calendarId = googleCalendarSettings.getCalendarId();
        String clientId = googleCalendarSettings.getClientId();
        String clientSecret = googleCalendarSettings.getClientSecret();

        validateMandatoryTextField(calendarId, "calendarSettings.googleCalendarSettings.calendarId", errors);
        validateMandatoryTextField(clientId, "calendarSettings.googleCalendarSettings.clientId", errors);
        validateMandatoryTextField(clientSecret, "calendarSettings.googleCalendarSettings.clientSecret", errors);
    }

    private void validateExchangeEmail(ExchangeCalendarSettings exchangeCalendarSettings, Errors errors) {

        String emailAttribute = "calendarSettings.exchangeCalendarSettings.email";
        String email = exchangeCalendarSettings.getEmail();

        validateMandatoryMailAddress(email, emailAttribute, errors);
    }

    private void validateExchangePassword(ExchangeCalendarSettings exchangeCalendarSettings, Errors errors) {

        String passwordAttribute = "calendarSettings.exchangeCalendarSettings.password";
        String password = exchangeCalendarSettings.getPassword();

        validateMandatoryTextField(password, passwordAttribute, errors);
    }

    private void validateExchangeCalendarName(ExchangeCalendarSettings exchangeCalendarSettings, Errors errors) {

        String calendarAttribute = "calendarSettings.exchangeCalendarSettings.calendar";
        String calendar = exchangeCalendarSettings.getPassword();

        validateMandatoryTextField(calendar, calendarAttribute, errors);
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

    private void validateMandatoryMailAddress(String mailAddress, String attributeName, Errors errors) {

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

    private boolean validStringLength(String string) {
        return string.length() <= MAX_CHARS;
    }
}
