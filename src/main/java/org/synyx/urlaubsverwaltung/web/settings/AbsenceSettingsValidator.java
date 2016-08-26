package org.synyx.urlaubsverwaltung.web.settings;

import org.springframework.stereotype.Component;

import org.springframework.util.Assert;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.settings.AbsenceSettings;


/**
 * Validates {@link org.synyx.urlaubsverwaltung.core.settings.AbsenceSettings}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Component
class AbsenceSettingsValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_ENTRY = "error.entry.invalid";

    private static final int DAYS_PER_YEAR = 366;

    @Override
    public boolean supports(Class<?> clazz) {

        return clazz.equals(AbsenceSettings.class);
    }


    @Override
    public void validate(Object target, Errors errors) {

        Assert.isTrue(supports(target.getClass()), "The given object must be an instance of AbsenceSettings");

        AbsenceSettings settings = (AbsenceSettings) target;

        validateVacationSettings(settings, errors);

        validateSickNoteSettings(settings, errors);
    }


    private void validateVacationSettings(AbsenceSettings settings, Errors errors) {

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

        Integer daysBeforeRemindForWaitingApplications = settings.getDaysBeforeRemindForWaitingApplications();

        if (daysBeforeRemindForWaitingApplications == null) {
            errors.rejectValue("daysBeforeRemindForWaitingApplications", ERROR_MANDATORY_FIELD);
        } else if (daysBeforeRemindForWaitingApplications <= 0) {
            errors.rejectValue("daysBeforeRemindForWaitingApplications", ERROR_INVALID_ENTRY);
        }
    }


    private void validateSickNoteSettings(AbsenceSettings settings, Errors errors) {

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
}
