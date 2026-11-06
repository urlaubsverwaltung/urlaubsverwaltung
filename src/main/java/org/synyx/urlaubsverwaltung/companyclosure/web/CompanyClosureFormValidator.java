package org.synyx.urlaubsverwaltung.companyclosure.web;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriod;
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriodService;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureEntry;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureService;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.MonthDay;
import java.time.Year;
import java.util.Comparator;
import java.util.stream.IntStream;

import static java.time.Month.FEBRUARY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;
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

    private static final String ERROR_BLACKOUT_PERIOD = "company-closure.error.blackout-period";

    private static final String ATTRIBUTE_DAY = "day";
    private static final String ATTRIBUTE_MONTH = "month";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_DAY_LENGTH = "dayLength";
    private static final String ATTRIBUTE_VALID_FROM = "validFrom";

    private final CompanyClosureService companyClosureService;
    private final BlackoutPeriodService blackoutPeriodService;
    private final Clock clock;

    CompanyClosureFormValidator(CompanyClosureService companyClosureService, BlackoutPeriodService blackoutPeriodService, Clock clock) {
        this.companyClosureService = companyClosureService;
        this.blackoutPeriodService = blackoutPeriodService;
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
     * Validates the changes of the closure day with the given day and month: the fields of
     * {@link #validate(Object, Errors)} plus the company-wide blackout periods, which only block a change that
     * closes more of the day than the version valid at "valid from" does.
     */
    void validateEdit(CompanyClosureEntry entry, CompanyClosureForm form, Errors errors) {
        validate(form, errors);
        if (form.getDayLength() == null || form.getValidFrom() == null) {
            return;
        }
        final DayLength old = entry.dayLengthAt(form.getValidFrom());
        final DayLength changed = form.getDayLength();
        final boolean adds = (covers(changed, MORNING) && !covers(old, MORNING)) || (covers(changed, NOON) && !covers(old, NOON));
        if (adds) {
            validateNoCompanyWideBlackoutPeriod(entry.monthDay(), form, errors);
        }
    }

    private static boolean covers(DayLength dayLength, DayLength half) {
        return dayLength == FULL || dayLength == half;
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
        } else {
            validateNoCompanyWideBlackoutPeriod(monthDay, form, errors);
        }
    }

    /**
     * A closure day recurs every year from "valid from" on, so a company-wide blackout period in any of these
     * years containing the day blocks it. Removing the closure ({@code ZERO}) is never blocked.
     */
    private void validateNoCompanyWideBlackoutPeriod(MonthDay monthDay, CompanyClosureForm form, Errors errors) {
        if (form.getDayLength() == null || form.getDayLength() == ZERO || form.getValidFrom() == null) {
            return;
        }

        blackoutPeriodService.getAllBlackoutPeriods().stream()
            .filter(BlackoutPeriod::isCompanyWide)
            .sorted(Comparator.comparing(BlackoutPeriod::getStartDate))
            .filter(period -> IntStream.rangeClosed(period.getStartDate().getYear(), period.getEndDate().getYear())
                .mapToObj(monthDay::atYear)
                .anyMatch(date -> !date.isBefore(form.getValidFrom()) && !date.isBefore(period.getStartDate()) && !date.isAfter(period.getEndDate())))
            .findFirst()
            .ifPresent(period -> errors.reject(ERROR_BLACKOUT_PERIOD, new Object[]{period.getTitle()}, null));
    }
}
