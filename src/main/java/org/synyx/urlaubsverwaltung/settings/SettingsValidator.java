package org.synyx.urlaubsverwaltung.settings;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.absence.AbsenceSettings;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.account.AccountSettings;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.exchange.ExchangeCalendarProvider;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.google.GoogleCalendarSyncProvider;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteSettings;
import org.synyx.urlaubsverwaltung.web.MailAddressValidationUtil;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;


@Component
public class SettingsValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_ENTRY = "error.entry.invalid";
    private static final String ERROR_INVALID_EMAIL = "error.entry.mail";
    private static final String ERROR_LENGTH = "error.entry.tooManyChars";

    private static final int DAYS_PER_YEAR = 366;
    private static final int HOURS_PER_DAY = 24;
    private static final int MAX_CHARS = 255;


    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(Settings.class);
    }

    @Override
    public void validate(Object o, Errors errors) {

        Assert.isTrue(supports(o.getClass()), "The given object must be an instance of Settings");

        final Settings settings = (Settings) o;
        validatePublicHolidaysSettings(settings, errors);
        validateOvertimeSettings(settings, errors);
        validateAbsenceSettings(settings, errors);
        validateAccountSettings(settings, errors);
        validateSickNoteSettings(settings, errors);
        validateCalendarSettings(settings, errors);
        validateTimeSettings(settings, errors);
    }

    private void validatePublicHolidaysSettings(Settings settings, Errors errors) {

        final WorkingTimeSettings workingTimeSettings = settings.getWorkingTimeSettings();

        if (workingTimeSettings.getFederalState() == null) {
            errors.rejectValue("workingTimeSettings.federalState", ERROR_MANDATORY_FIELD);
        }

        if (workingTimeSettings.getWorkingDurationForChristmasEve() == null) {
            errors.rejectValue("workingTimeSettings.workingDurationForChristmasEve", ERROR_MANDATORY_FIELD);
        }

        if (workingTimeSettings.getWorkingDurationForNewYearsEve() == null) {
            errors.rejectValue("workingTimeSettings.workingDurationForNewYearsEve", ERROR_MANDATORY_FIELD);
        }
    }

    private void validateOvertimeSettings(Settings settings, Errors errors) {

        final OvertimeSettings overtimeSettings = settings.getOvertimeSettings();
        if (!overtimeSettings.isOvertimeActive()) {
            return;
        }

        validateOvertimeLimit(overtimeSettings.getMaximumOvertime(), "overtimeSettings.maximumOvertime", errors);
        validateOvertimeLimit(overtimeSettings.getMinimumOvertime(), "overtimeSettings.minimumOvertime", errors);
    }

    private void validateOvertimeLimit(Integer limit, String field, Errors errors) {

        if (limit == null) {
            errors.rejectValue(field, ERROR_MANDATORY_FIELD);
            return;
        }

        if (limit < 0) {
            errors.rejectValue(field, ERROR_INVALID_ENTRY);
        }
    }

    private void validateAbsenceSettings(Settings settings, Errors errors) {

        final AbsenceSettings absenceSettings = settings.getAbsenceSettings();

        final Integer maximumMonthsToApplyForLeaveInAdvance = absenceSettings.getMaximumMonthsToApplyForLeaveInAdvance();
        if (maximumMonthsToApplyForLeaveInAdvance == null) {
            errors.rejectValue("absenceSettings.maximumMonthsToApplyForLeaveInAdvance", ERROR_MANDATORY_FIELD);
        } else if (maximumMonthsToApplyForLeaveInAdvance <= 0) {
            errors.rejectValue("absenceSettings.maximumMonthsToApplyForLeaveInAdvance", ERROR_INVALID_ENTRY);
        }

        final Integer daysBeforeRemindForWaitingApplications = absenceSettings.getDaysBeforeRemindForWaitingApplications();
        if (daysBeforeRemindForWaitingApplications == null) {
            errors.rejectValue("absenceSettings.daysBeforeRemindForWaitingApplications", ERROR_MANDATORY_FIELD);
        } else if (daysBeforeRemindForWaitingApplications <= 0) {
            errors.rejectValue("absenceSettings.daysBeforeRemindForWaitingApplications", ERROR_INVALID_ENTRY);
        }
    }

    private void validateAccountSettings(Settings settings, Errors errors) {

        final AccountSettings accountSettings = settings.getAccountSettings();

        final Integer maximumAnnualVacationDays = accountSettings.getMaximumAnnualVacationDays();
        if (maximumAnnualVacationDays == null) {
            errors.rejectValue("accountSettings.maximumAnnualVacationDays", ERROR_MANDATORY_FIELD);
        } else if (maximumAnnualVacationDays < 0 || maximumAnnualVacationDays > DAYS_PER_YEAR) {
            errors.rejectValue("accountSettings.maximumAnnualVacationDays", ERROR_INVALID_ENTRY);
        }
    }

    private void validateSickNoteSettings(Settings settings, Errors errors) {

        final SickNoteSettings sickNoteSettings = settings.getSickNoteSettings();

        final Integer maximumSickPayDays = sickNoteSettings.getMaximumSickPayDays();
        final Integer daysBeforeEndOfSickPayNotification = sickNoteSettings.getDaysBeforeEndOfSickPayNotification();

        if (maximumSickPayDays == null) {
            errors.rejectValue("sickNoteSettings.maximumSickPayDays", ERROR_MANDATORY_FIELD);
        } else if (maximumSickPayDays < 0) {
            errors.rejectValue("sickNoteSettings.maximumSickPayDays", ERROR_INVALID_ENTRY);
        }

        if (daysBeforeEndOfSickPayNotification == null) {
            errors.rejectValue("sickNoteSettings.daysBeforeEndOfSickPayNotification", ERROR_MANDATORY_FIELD);
        } else if (daysBeforeEndOfSickPayNotification < 0) {
            errors.rejectValue("sickNoteSettings.daysBeforeEndOfSickPayNotification", ERROR_INVALID_ENTRY);
        }

        if (maximumSickPayDays != null && daysBeforeEndOfSickPayNotification != null
            && daysBeforeEndOfSickPayNotification > maximumSickPayDays) {
            errors.rejectValue("sickNoteSettings.daysBeforeEndOfSickPayNotification",
                "settings.sickDays.daysBeforeEndOfSickPayNotification.error");
        }
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

    private void validateCalendarSettings(Settings settings, Errors errors) {

        final CalendarSettings calendarSettings = settings.getCalendarSettings();

        if (calendarSettings.getProvider().equals(ExchangeCalendarProvider.class.getSimpleName())) {
            validateExchangeCalendarSettings(calendarSettings.getExchangeCalendarSettings(), errors);
        }
        if (calendarSettings.getProvider().equals(GoogleCalendarSyncProvider.class.getSimpleName())) {
            validateGoogleCalendarSettings(calendarSettings.getGoogleCalendarSettings(), errors);
        }
    }

    private void validateTimeSettings(Settings settings, Errors errors) {

        final TimeSettings timeSettings = settings.getTimeSettings();

        final String workDayBeginHourAttribute = "timeSettings.workDayBeginHour";
        final Integer workDayBeginHour = timeSettings.getWorkDayBeginHour();
        validateWorkDayHour(workDayBeginHour, workDayBeginHourAttribute, errors);

        final Integer workDayEndHour = timeSettings.getWorkDayEndHour();
        final String workDayEndHourAttribute = "timeSettings.workDayEndHour";
        validateWorkDayHour(workDayEndHour, workDayEndHourAttribute, errors);

        final boolean beginHourValid = workDayBeginHour != null && isValidWorkDayHour(workDayBeginHour);
        final boolean endHourValid = workDayEndHour != null && isValidWorkDayHour(workDayEndHour);
        final boolean beginAndEndValid = beginHourValid && endHourValid;

        if (beginAndEndValid && workDayBeginHour >= workDayEndHour) {
            errors.rejectValue(workDayBeginHourAttribute, ERROR_INVALID_ENTRY);
            errors.rejectValue(workDayEndHourAttribute, ERROR_INVALID_ENTRY);
        }
    }

    private boolean isValidWorkDayHour(int workDayHour) {
        return workDayHour > 0 && workDayHour <= HOURS_PER_DAY;
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
}
