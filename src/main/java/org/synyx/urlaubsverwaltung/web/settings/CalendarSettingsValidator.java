package org.synyx.urlaubsverwaltung.web.settings;

import org.springframework.stereotype.Component;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.ExchangeCalendarSettings;
import org.synyx.urlaubsverwaltung.web.MailAddressValidationUtil;


/**
 * Validates {@link org.synyx.urlaubsverwaltung.core.settings.CalendarSettings}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Component
class CalendarSettingsValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_ENTRY = "error.entry.invalid";
    private static final String ERROR_INVALID_EMAIL = "error.entry.mail";
    private static final String ERROR_LENGTH = "error.entry.tooManyChars";

    private static final int HOURS_PER_DAY = 24;
    private static final int MAX_CHARS = 255;

    @Override
    public boolean supports(Class<?> clazz) {

        return clazz.equals(CalendarSettings.class);
    }


    @Override
    public void validate(Object target, Errors errors) {

        Assert.isTrue(supports(target.getClass()), "The given object must be an instance of CalendarSettings");

        CalendarSettings settings = (CalendarSettings) target;

        validateCalendarSettings(settings, errors);

        validateExchangeCalendarSettings(settings.getExchangeCalendarSettings(), errors);
    }


    private void validateCalendarSettings(CalendarSettings calendarSettings, Errors errors) {

        String workDayBeginHourAttribute = "workDayBeginHour";
        Integer workDayBeginHour = calendarSettings.getWorkDayBeginHour();
        validateWorkDayHour(workDayBeginHour, workDayBeginHourAttribute, errors);

        Integer workDayEndHour = calendarSettings.getWorkDayEndHour();
        String workDayEndHourAttribute = "workDayEndHour";
        validateWorkDayHour(workDayEndHour, workDayEndHourAttribute, errors);

        boolean beginHourValid = workDayBeginHour != null && isValidWorkDayHour(workDayBeginHour);
        boolean endHourValid = workDayEndHour != null && isValidWorkDayHour(workDayEndHour);
        boolean beginAndEndValid = beginHourValid && endHourValid;

        if (beginAndEndValid && workDayBeginHour >= workDayEndHour) {
            errors.rejectValue(workDayBeginHourAttribute, ERROR_INVALID_ENTRY);
            errors.rejectValue(workDayEndHourAttribute, ERROR_INVALID_ENTRY);
        }
    }


    private void validateWorkDayHour(Integer workDayHour, String attribute, Errors errors) {

        if (workDayHour == null) {
            errors.rejectValue(attribute, ERROR_MANDATORY_FIELD);
        } else {
            if (!isValidWorkDayHour(workDayHour)) {
                errors.rejectValue(attribute, ERROR_INVALID_ENTRY);
            }
        }
    }


    private boolean isValidWorkDayHour(int workDayHour) {

        return workDayHour > 0 && workDayHour <= HOURS_PER_DAY;
    }


    private void validateExchangeCalendarSettings(ExchangeCalendarSettings exchangeCalendarSettings, Errors errors) {

        if (exchangeCalendarSettings.isActive()) {
            validateExchangeEmail(exchangeCalendarSettings, errors);
            validateExchangePassword(exchangeCalendarSettings, errors);
            validateExchangeCalendarName(exchangeCalendarSettings, errors);
        }
    }


    private void validateExchangeEmail(ExchangeCalendarSettings exchangeCalendarSettings, Errors errors) {

        String emailAttribute = "exchangeCalendarSettings.email";
        String email = exchangeCalendarSettings.getEmail();

        validateMandatoryMailAddress(email, emailAttribute, errors);
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


    private void validateExchangePassword(ExchangeCalendarSettings exchangeCalendarSettings, Errors errors) {

        String passwordAttribute = "exchangeCalendarSettings.password";
        String password = exchangeCalendarSettings.getPassword();

        validateMandatoryTextField(password, passwordAttribute, errors);
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


    private void validateExchangeCalendarName(ExchangeCalendarSettings exchangeCalendarSettings, Errors errors) {

        String calendarAttribute = "exchangeCalendarSettings.calendar";
        String calendar = exchangeCalendarSettings.getPassword();

        validateMandatoryTextField(calendar, calendarAttribute, errors);
    }
}
