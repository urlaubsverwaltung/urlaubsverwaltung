package org.synyx.urlaubsverwaltung.web.settings;

import org.springframework.stereotype.Component;

import org.springframework.util.Assert;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.settings.WorkingTimeSettings;


/**
 * Validates {@link org.synyx.urlaubsverwaltung.core.settings.WorkingTimeSettings}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Component
class WorkingTimeSettingsValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_ENTRY = "error.entry.invalid";

    @Override
    public boolean supports(Class<?> clazz) {

        return clazz.equals(WorkingTimeSettings.class);
    }


    @Override
    public void validate(Object target, Errors errors) {

        Assert.isTrue(supports(target.getClass()), "The given object must be an instance of WorkingTimeSettings");

        WorkingTimeSettings workingTimeSettings = (WorkingTimeSettings) target;

        validatePublicHolidaysSettings(workingTimeSettings, errors);

        validateOvertimeSettings(workingTimeSettings, errors);
    }


    private void validatePublicHolidaysSettings(WorkingTimeSettings workingTimeSettings, Errors errors) {

        if (workingTimeSettings.getFederalState() == null) {
            errors.rejectValue("federalState", ERROR_MANDATORY_FIELD);
        }

        if (workingTimeSettings.getWorkingDurationForChristmasEve() == null) {
            errors.rejectValue("workingDurationForChristmasEve", ERROR_MANDATORY_FIELD);
        }

        if (workingTimeSettings.getWorkingDurationForNewYearsEve() == null) {
            errors.rejectValue("workingDurationForNewYearsEve", ERROR_MANDATORY_FIELD);
        }
    }


    private void validateOvertimeSettings(WorkingTimeSettings workingTimeSettings, Errors errors) {

        if (!workingTimeSettings.isOvertimeActive()) {
            return;
        }

        validateOvertimeLimit(workingTimeSettings.getMaximumOvertime(), "maximumOvertime", errors);
        validateOvertimeLimit(workingTimeSettings.getMinimumOvertime(), "minimumOvertime", errors);
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
}
