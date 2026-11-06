package org.synyx.urlaubsverwaltung.companyclosure.web;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureEntry;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureService;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.MonthDay;
import java.time.Year;

import static java.time.Month.FEBRUARY;
import static org.springframework.util.StringUtils.hasText;

@Component
class CompanyClosureFormValidator implements Validator {

    // the name column is unbounded text, the limit keeps names readable in lists and calendars
    private static final int MAX_NAME_LENGTH = 100;

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_TOO_MANY_CHARS = "error.entry.tooManyChars";
    private static final String ERROR_VALID_FROM_PREVIOUS_YEAR = "company-closure.error.validFrom.previousYear";
    private static final String ERROR_MONTH_DAY_INVALID = "company-closure.error.monthDay.invalid";
    private static final String ERROR_MONTH_DAY_LEAP_DAY = "company-closure.error.monthDay.leapDay";
    private static final String ERROR_MONTH_DAY_EXISTS = "company-closure.error.monthDay.exists";

    private static final String ATTRIBUTE_DAY = "day";
    private static final String ATTRIBUTE_MONTH = "month";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_DAY_LENGTH = "dayLength";
    private static final String ATTRIBUTE_VALID_FROM = "validFrom";

    private final CompanyClosureService companyClosureService;
    private final Clock clock;

    CompanyClosureFormValidator(CompanyClosureService companyClosureService, Clock clock) {
        this.companyClosureService = companyClosureService;
        this.clock = clock;
    }

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return CompanyClosureForm.class.equals(clazz);
    }

    /**
     * Validates the fields of an existing closure day: name, day length and valid from.
     */
    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {

        final CompanyClosureForm form = (CompanyClosureForm) target;

        if (!hasText(form.getName())) {
            errors.rejectValue(ATTRIBUTE_NAME, ERROR_MANDATORY_FIELD);
        } else if (form.getName().length() > MAX_NAME_LENGTH) {
            errors.rejectValue(ATTRIBUTE_NAME, ERROR_TOO_MANY_CHARS);
        }

        if (form.getDayLength() == null) {
            errors.rejectValue(ATTRIBUTE_DAY_LENGTH, ERROR_MANDATORY_FIELD);
        }

        if (form.getValidFrom() == null) {
            errors.rejectValue(ATTRIBUTE_VALID_FROM, ERROR_MANDATORY_FIELD);
        } else if (form.getValidFrom().isBefore(Year.now(clock).atDay(1))) {
            errors.rejectValue(ATTRIBUTE_VALID_FROM, ERROR_VALID_FROM_PREVIOUS_YEAR);
        }
    }

    /**
     * Validates a new closure day: the fields of {@link #validate(Object, Errors)} plus day and month.
     */
    void validateNew(CompanyClosureForm form, Errors errors) {

        validate(form, errors);

        if (form.getDay() == null) {
            errors.rejectValue(ATTRIBUTE_DAY, ERROR_MANDATORY_FIELD);
        }
        if (form.getMonth() == null) {
            errors.rejectValue(ATTRIBUTE_MONTH, ERROR_MANDATORY_FIELD);
        }
        if (form.getDay() == null || form.getMonth() == null) {
            return;
        }

        final MonthDay monthDay;
        try {
            monthDay = MonthDay.of(form.getMonth(), form.getDay());
        } catch (DateTimeException e) {
            errors.rejectValue(ATTRIBUTE_DAY, ERROR_MONTH_DAY_INVALID);
            return;
        }

        if (monthDay.equals(MonthDay.of(FEBRUARY, 29))) {
            errors.rejectValue(ATTRIBUTE_DAY, ERROR_MONTH_DAY_LEAP_DAY);
        } else if (companyClosureService.getCompanyClosureEntries().stream().map(CompanyClosureEntry::monthDay).anyMatch(monthDay::equals)) {
            errors.rejectValue(ATTRIBUTE_DAY, ERROR_MONTH_DAY_EXISTS);
        }
    }
}
