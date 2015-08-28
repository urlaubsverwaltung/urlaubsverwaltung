package org.synyx.urlaubsverwaltung.web.validator;

import org.springframework.stereotype.Component;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.settings.MailSettings;
import org.synyx.urlaubsverwaltung.core.settings.Settings;


/**
 * Daniel Hammann - <hammann@synyx.de>.
 */
@Component
public class SettingsValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_ENTRY = "error.entry.invalid";
    private static final String ERROR_INVALID_EMAIL = "error.entry.mail";
    private static final String ERROR_LENGTH = "error.entry.tooManyChars";

    private static final int DAYS_PER_YEAR = 366;
    private static final int MAX_CHARS = 255;

    @Override
    public boolean supports(Class<?> clazz) {

        return clazz.equals(Settings.class);
    }


    @Override
    public void validate(Object o, Errors errors) {

        Assert.isTrue(supports(o.getClass()), "The given object must be an instance of Settings");

        Settings settings = (Settings) o;

        validatePublicHolidaysSettings(settings, errors);

        validateVacationSettings(settings, errors);

        validateSickNoteSettings(settings, errors);

        validateMailSettings(settings, errors);
    }


    private void validatePublicHolidaysSettings(Settings settings, Errors errors) {

        if (settings.getFederalState() == null) {
            errors.rejectValue("federalState", ERROR_MANDATORY_FIELD);
        }

        if (settings.getWorkingDurationForChristmasEve() == null) {
            errors.rejectValue("workingDurationForChristmasEve", ERROR_MANDATORY_FIELD);
        }

        if (settings.getWorkingDurationForNewYearsEve() == null) {
            errors.rejectValue("workingDurationForNewYearsEve", ERROR_MANDATORY_FIELD);
        }
    }


    private void validateVacationSettings(Settings settings, Errors errors) {

        Integer maximumAnnualVacationDays = settings.getMaximumAnnualVacationDays();

        if (maximumAnnualVacationDays == null) {
            errors.rejectValue("maximumAnnualVacationDays", ERROR_MANDATORY_FIELD);
        } else if (maximumAnnualVacationDays < 0 || maximumAnnualVacationDays > DAYS_PER_YEAR) {
            errors.rejectValue("maximumAnnualVacationDays", ERROR_INVALID_ENTRY);
        }

        Integer maximumMonthsToApplyForLeaveInAdvance = settings.getMaximumMonthsToApplyForLeaveInAdvance();

        if (maximumMonthsToApplyForLeaveInAdvance == null) {
            errors.rejectValue("maximumMonthsToApplyForLeaveInAdvance", ERROR_MANDATORY_FIELD);
        } else if (maximumMonthsToApplyForLeaveInAdvance <= 0) {
            errors.rejectValue("maximumMonthsToApplyForLeaveInAdvance", ERROR_INVALID_ENTRY);
        }
    }


    private void validateSickNoteSettings(Settings settings, Errors errors) {

        Integer maximumSickPayDays = settings.getMaximumSickPayDays();
        Integer daysBeforeEndOfSickPayNotification = settings.getDaysBeforeEndOfSickPayNotification();

        if (maximumSickPayDays == null) {
            errors.rejectValue("maximumSickPayDays", ERROR_MANDATORY_FIELD);
        } else if (maximumSickPayDays < 0) {
            errors.rejectValue("maximumSickPayDays", ERROR_INVALID_ENTRY);
        }

        if (daysBeforeEndOfSickPayNotification == null) {
            errors.rejectValue("daysBeforeEndOfSickPayNotification", ERROR_MANDATORY_FIELD);
        } else if (daysBeforeEndOfSickPayNotification < 0) {
            errors.rejectValue("daysBeforeEndOfSickPayNotification", ERROR_INVALID_ENTRY);
        }

        if (maximumSickPayDays != null && daysBeforeEndOfSickPayNotification != null
                && daysBeforeEndOfSickPayNotification > maximumSickPayDays) {
            errors.rejectValue("daysBeforeEndOfSickPayNotification",
                "settings.sickDays.daysBeforeEndOfSickPayNotification.error");
        }
    }


    private void validateMailSettings(Settings settings, Errors errors) {

        MailSettings mailSettings = settings.getMailSettings();

        validateMailHost(mailSettings, errors);

        validateMailPort(mailSettings, errors);

        validateMailUsername(mailSettings, errors);

        validateMailPassword(mailSettings, errors);

        validateMailFrom(mailSettings, errors);

        validateMailAdministrator(mailSettings, errors);
    }


    private void validateMailHost(MailSettings mailSettings, Errors errors) {

        String hostAttribute = "mailSettings.host";
        String host = mailSettings.getHost();

        if (!StringUtils.hasText(host)) {
            if (mailSettings.isActive()) {
                errors.rejectValue(hostAttribute, ERROR_MANDATORY_FIELD);
            }
        } else {
            if (!validStringLength(host)) {
                errors.rejectValue(hostAttribute, ERROR_LENGTH);
            }
        }
    }


    private boolean validStringLength(String string) {

        return string.length() <= MAX_CHARS;
    }


    private void validateMailPort(MailSettings mailSettings, Errors errors) {

        String portAttribute = "mailSettings.port";
        Integer port = mailSettings.getPort();

        if (port == null) {
            if (mailSettings.isActive()) {
                errors.rejectValue(portAttribute, ERROR_MANDATORY_FIELD);
            }
        } else {
            if (port <= 0) {
                errors.rejectValue(portAttribute, ERROR_INVALID_ENTRY);
            }
        }
    }


    private void validateMailUsername(MailSettings mailSettings, Errors errors) {

        String username = mailSettings.getUsername();

        if (username != null && !validStringLength(username)) {
            errors.rejectValue("mailSettings.username", ERROR_LENGTH);
        }
    }


    private void validateMailPassword(MailSettings mailSettings, Errors errors) {

        String password = mailSettings.getPassword();

        if (password != null && !validStringLength(password)) {
            errors.rejectValue("mailSettings.password", ERROR_LENGTH);
        }
    }


    private void validateMailFrom(MailSettings mailSettings, Errors errors) {

        String fromAttribute = "mailSettings.from";
        String from = mailSettings.getFrom();

        if (!StringUtils.hasText(from)) {
            if (mailSettings.isActive()) {
                errors.rejectValue(fromAttribute, ERROR_MANDATORY_FIELD);
            }
        } else {
            if (!validStringLength(from)) {
                errors.rejectValue(fromAttribute, ERROR_LENGTH);
            }

            if (!MailAddressValidationUtil.hasValidFormat(from)) {
                errors.rejectValue(fromAttribute, ERROR_INVALID_EMAIL);
            }
        }
    }


    private void validateMailAdministrator(MailSettings mailSettings, Errors errors) {

        String administratorAttribute = "mailSettings.administrator";
        String administrator = mailSettings.getAdministrator();

        if (!StringUtils.hasText(administrator)) {
            if (mailSettings.isActive()) {
                errors.rejectValue(administratorAttribute, ERROR_MANDATORY_FIELD);
            }
        } else {
            if (!validStringLength(administrator)) {
                errors.rejectValue(administratorAttribute, ERROR_LENGTH);
            }

            if (!MailAddressValidationUtil.hasValidFormat(administrator)) {
                errors.rejectValue(administratorAttribute, ERROR_INVALID_EMAIL);
            }
        }
    }
}
