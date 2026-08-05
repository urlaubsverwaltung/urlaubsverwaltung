package org.synyx.urlaubsverwaltung.blackoutperiod.web;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.springframework.util.StringUtils.hasText;

@Component
class BlackoutPeriodFormValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_PERIOD = "error.entry.invalidPeriod";

    private static final String ATTRIBUTE_TITLE = "title";
    private static final String ATTRIBUTE_START_DATE = "startDate";
    private static final String ATTRIBUTE_END_DATE = "endDate";

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return BlackoutPeriodForm.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {

        final BlackoutPeriodForm form = (BlackoutPeriodForm) target;

        if (!hasText(form.getTitle())) {
            errors.rejectValue(ATTRIBUTE_TITLE, ERROR_MANDATORY_FIELD);
        }

        final boolean startDateMissing = form.getStartDate() == null;
        final boolean endDateMissing = form.getEndDate() == null;

        if (startDateMissing) {
            errors.rejectValue(ATTRIBUTE_START_DATE, ERROR_MANDATORY_FIELD);
        }

        if (endDateMissing) {
            errors.rejectValue(ATTRIBUTE_END_DATE, ERROR_MANDATORY_FIELD);
        }

        if (!startDateMissing && !endDateMissing && form.getStartDate().isAfter(form.getEndDate())) {
            errors.reject(ERROR_PERIOD);
        }
    }
}
