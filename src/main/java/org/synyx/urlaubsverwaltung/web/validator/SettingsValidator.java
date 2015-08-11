package org.synyx.urlaubsverwaltung.web.validator;

import org.springframework.stereotype.Component;

import org.springframework.util.Assert;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.settings.Settings;


/**
 * Daniel Hammann - <hammann@synyx.de>.
 */
@Component
public class SettingsValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_ENTRY = "error.entry.invalid";

    @Override
    public boolean supports(Class<?> clazz) {

        return clazz.equals(Settings.class);
    }


    @Override
    public void validate(Object o, Errors errors) {

        Assert.isTrue(supports(o.getClass()), "The given object must be an instance of Settings");

        Settings settings = (Settings) o;

        Integer maximumAnnualVacationDays = settings.getMaximumAnnualVacationDays();
        Integer maximumMonthsToApplyForLeaveInAdvance = settings.getMaximumMonthsToApplyForLeaveInAdvance();
        Integer maximumSickPayDays = settings.getMaximumSickPayDays();
        Integer daysBeforeEndOfSickPayNotification = settings.getDaysBeforeEndOfSickPayNotification();

        if (settings.getFederalState() == null) {
            errors.rejectValue("federalState", ERROR_MANDATORY_FIELD);
        }

        if (maximumAnnualVacationDays == null) {
            errors.rejectValue("maximumAnnualVacationDays", ERROR_MANDATORY_FIELD);
        } else if (maximumAnnualVacationDays < 0 || maximumAnnualVacationDays >= 367) {
            errors.rejectValue("maximumAnnualVacationDays", ERROR_INVALID_ENTRY);
        }

        if (maximumMonthsToApplyForLeaveInAdvance == null) {
            errors.rejectValue("maximumMonthsToApplyForLeaveInAdvance", ERROR_MANDATORY_FIELD);
        } else if (maximumMonthsToApplyForLeaveInAdvance <= 0) {
            errors.rejectValue("maximumMonthsToApplyForLeaveInAdvance", ERROR_INVALID_ENTRY);
        }

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
                "settings.daysBeforeEndOfSickPayNotification.error");
        }

        if (settings.getWorkingDurationForChristmasEve() == null) {
            errors.rejectValue("workingDurationForChristmasEve", ERROR_MANDATORY_FIELD);
        }

        if (settings.getWorkingDurationForNewYearsEve() == null) {
            errors.rejectValue("workingDurationForNewYearsEve", ERROR_MANDATORY_FIELD);
        }
    }
}
