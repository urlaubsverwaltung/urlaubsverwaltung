package org.synyx.urlaubsverwaltung.companyclosure.web;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriod;
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriodService;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureEntry;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureService;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureVersion;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.Clock;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.util.List;

import static java.time.Month.FEBRUARY;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.IntStream.rangeClosed;
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
    private static final String ERROR_NAMES_MANDATORY = "company-closure.error.names.mandatory";
    private static final String ERROR_VALID_FROM_PREVIOUS_YEAR = "company-closure.error.validFrom.previousYear";
    private static final String ERROR_DAY_RANGE = "company-closure.error.day.range";
    private static final String ERROR_MONTH_RANGE = "company-closure.error.month.range";
    private static final String ERROR_MONTH_DAY_INVALID = "company-closure.error.monthDay.invalid";
    private static final String ERROR_MONTH_DAY_LEAP_DAY = "company-closure.error.monthDay.leapDay";
    private static final String ERROR_MONTH_DAY_EXISTS = "company-closure.error.monthDay.exists";

    private static final String ERROR_BLACKOUT_PERIOD = "company-closure.error.blackout-period";

    private static final int MAX_DAY = 31;
    private static final int MAX_MONTH = 12;

    private static final String ATTRIBUTE_DAY = "day";
    private static final String ATTRIBUTE_MONTH = "month";
    // errors that concern day and month together
    private static final String ATTRIBUTE_MONTH_DAY = "monthDay";
    private static final String ATTRIBUTE_NAMES = "names";
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
     * Validates the fields of an existing closure day: names, day length and valid from.
     */
    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {

        final CompanyClosureForm form = (CompanyClosureForm) target;

        validateNames(form, errors);

        if (form.getDayLength() == null) {
            errors.rejectValue(ATTRIBUTE_DAY_LENGTH, ERROR_MANDATORY_FIELD);
        }

        if (form.getValidFrom() == null) {
            errors.rejectValue(ATTRIBUTE_VALID_FROM, ERROR_MANDATORY_FIELD);
        } else if (form.getValidFrom().isBefore(Year.now(clock).atDay(1))) {
            errors.rejectValue(ATTRIBUTE_VALID_FROM, ERROR_VALID_FROM_PREVIOUS_YEAR);
        }
    }

    private static void validateNames(CompanyClosureForm form, Errors errors) {
        final List<CompanyClosureNameForm> names = form.getNames();
        if (names == null || names.stream().noneMatch(name -> hasText(name.getName()))) {
            errors.rejectValue(ATTRIBUTE_NAMES, ERROR_NAMES_MANDATORY);
            return;
        }

        for (int index = 0; index < names.size(); index++) {
            final String name = names.get(index).getName();
            if (name != null && name.length() > MAX_NAME_LENGTH) {
                errors.rejectValue("%s[%d].name".formatted(ATTRIBUTE_NAMES, index), ERROR_TOO_MANY_CHARS);
            }
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
            // the changed version only applies until the next one starts
            final LocalDate nextVersionStart = entry.versions().stream()
                .map(CompanyClosureVersion::validFrom)
                .filter(validFrom -> isAfter(validFrom, form.getValidFrom()))
                .min(naturalOrder())
                .orElse(null);
            validateNoCompanyWideBlackoutPeriod(entry.monthDay(), nextVersionStart, form, errors);
        }
    }

    private static boolean isAfter(@Nullable LocalDate validFrom, LocalDate date) {
        return validFrom != null && validFrom.isAfter(date);
    }

    private static boolean covers(DayLength dayLength, DayLength half) {
        return dayLength == FULL || dayLength == half;
    }

    /**
     * Validates a new closure day: the fields of {@link #validate(Object, Errors)} plus day and month.
     */
    void validateNew(CompanyClosureForm form, Errors errors) {

        validate(form, errors);

        validateNumber(ATTRIBUTE_DAY, form.getDay(), MAX_DAY, ERROR_DAY_RANGE, errors);
        validateNumber(ATTRIBUTE_MONTH, form.getMonth(), MAX_MONTH, ERROR_MONTH_RANGE, errors);
        if (errors.hasFieldErrors(ATTRIBUTE_DAY) || errors.hasFieldErrors(ATTRIBUTE_MONTH)) {
            return;
        }

        final MonthDay monthDay = form.getMonthDay();
        if (monthDay == null) {
            errors.rejectValue(ATTRIBUTE_MONTH_DAY, ERROR_MONTH_DAY_INVALID);
        } else if (monthDay.equals(MonthDay.of(FEBRUARY, 29))) {
            errors.rejectValue(ATTRIBUTE_MONTH_DAY, ERROR_MONTH_DAY_LEAP_DAY);
        } else if (companyClosureService.getCompanyClosureEntries().stream().map(CompanyClosureEntry::monthDay).anyMatch(monthDay::equals)) {
            errors.rejectValue(ATTRIBUTE_MONTH_DAY, ERROR_MONTH_DAY_EXISTS);
        } else {
            validateNoCompanyWideBlackoutPeriod(monthDay, null, form, errors);
        }
    }

    /**
     * Validates that the number is given and between 1 and {@code max}. A field with an error already, i.e. an input
     * that is not a number, is left as it is: its value is {@code null} without being missing.
     */
    private static void validateNumber(String field, @Nullable Integer value, int max, String rangeError, Errors errors) {
        if (errors.hasFieldErrors(field)) {
            return;
        }
        if (value == null) {
            errors.rejectValue(field, ERROR_MANDATORY_FIELD);
        } else if (value < 1 || value > max) {
            errors.rejectValue(field, rangeError);
        }
    }

    /**
     * A closure day recurs every year from "valid from" until the next version starts, so a company-wide blackout
     * period in any of these years containing the day blocks it. Removing the closure ({@code ZERO}) is never blocked.
     *
     * @param nextVersionStart exclusive end of the years to check, {@code null} if no later version exists
     */
    private void validateNoCompanyWideBlackoutPeriod(MonthDay monthDay, @Nullable LocalDate nextVersionStart, CompanyClosureForm form, Errors errors) {
        if (form.getDayLength() == null || form.getDayLength() == ZERO || form.getValidFrom() == null) {
            return;
        }

        blackoutPeriodService.getAllBlackoutPeriods().stream()
            .filter(BlackoutPeriod::isCompanyWide)
            .sorted(comparing(BlackoutPeriod::getStartDate))
            .filter(period -> containsClosureDay(period, monthDay, form.getValidFrom(), nextVersionStart))
            .findFirst()
            .ifPresent(period -> errors.reject(ERROR_BLACKOUT_PERIOD, new Object[]{period.getTitle()}, null));
    }

    /**
     * @return whether the period contains the closure day of a year the version applies to
     */
    private static boolean containsClosureDay(BlackoutPeriod period, MonthDay monthDay, LocalDate validFrom, @Nullable LocalDate nextVersionStart) {
        return rangeClosed(period.getStartDate().getYear(), period.getEndDate().getYear())
            .mapToObj(monthDay::atYear)
            .filter(date -> isInVersion(date, validFrom, nextVersionStart))
            .anyMatch(date -> isInPeriod(date, period));
    }

    private static boolean isInVersion(LocalDate date, LocalDate validFrom, @Nullable LocalDate nextVersionStart) {
        return !date.isBefore(validFrom) && (nextVersionStart == null || date.isBefore(nextVersionStart));
    }

    private static boolean isInPeriod(LocalDate date, BlackoutPeriod period) {
        return !date.isBefore(period.getStartDate()) && !date.isAfter(period.getEndDate());
    }
}
