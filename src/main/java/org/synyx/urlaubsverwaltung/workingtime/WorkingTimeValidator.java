package org.synyx.urlaubsverwaltung.workingtime;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


/**
 * Validates {@link WorkingTimeForm}.
 */
@Component
class WorkingTimeValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_WORKING_TIME_MANDATORY = "person.form.workingTime.error.mandatory";

    @Override
    public boolean supports(@NonNull Class<?> clazz) {

        return WorkingTimeForm.class.equals(clazz);
    }


    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {

        WorkingTimeForm form = (WorkingTimeForm) target;

        // may be that date field is null because of cast exception, than there is already a field error
        if (form.getValidFrom() == null && errors.getFieldErrors("validFrom").isEmpty()) {
            errors.rejectValue("validFrom", ERROR_MANDATORY_FIELD);
        }

        if (form.getWorkingDays() == null || form.getWorkingDays().isEmpty()) {
            errors.rejectValue("workingDays", ERROR_WORKING_TIME_MANDATORY);
        }
    }
}
