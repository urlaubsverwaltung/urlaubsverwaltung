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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.time.Month.FEBRUARY;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.stream.IntStream.rangeClosed;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;
import static org.springframework.util.StringUtils.hasText;

@Component
class CompanyClosureFormValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_NAMES_MANDATORY = "company-closure.error.names.mandatory";
    private static final String ERROR_VALID_FROM_PREVIOUS_YEAR = "company-closure.error.validFrom.previousYear";
    private static final String ERROR_DAY_RANGE = "company-closure.error.day.range";
    private static final String ERROR_MONTH_RANGE = "company-closure.error.month.range";
    private static final String ERROR_MONTH_DAY_INVALID = "company-closure.error.monthDay.invalid";
    private static final String ERROR_MONTH_DAY_LEAP_DAY = "company-closure.error.monthDay.leapDay";
    private static final String ERROR_MONTH_DAY_EXISTS = "company-closure.error.monthDay.exists";

    private static final String ERROR_VALID_FROM_DUPLICATE = "company-closure.error.validFrom.duplicate";
    private static final String ERROR_VERSION_NOT_EDITABLE = "company-closure.error.version.notEditable";

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
        return CompanyClosureFormDto.class.equals(clazz);
    }

    /**
     * Validates the fields of an existing closure day: names, day length and valid from.
     */
    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {

        final CompanyClosureFormDto form = (CompanyClosureFormDto) target;

        validateNames(form, errors);
        validateNewVersion(form, errors);
    }

    private void validateNewVersion(CompanyClosureFormDto form, Errors errors) {
        if (form.getDayLength() == null) {
            errors.rejectValue(ATTRIBUTE_DAY_LENGTH, ERROR_MANDATORY_FIELD);
        }

        if (form.getValidFrom() == null) {
            errors.rejectValue(ATTRIBUTE_VALID_FROM, ERROR_MANDATORY_FIELD);
        } else if (form.getValidFrom().isBefore(Year.now(clock).atDay(1))) {
            errors.rejectValue(ATTRIBUTE_VALID_FROM, ERROR_VALID_FROM_PREVIOUS_YEAR);
        }
    }

    private static void validateNames(CompanyClosureFormDto form, Errors errors) {
        final List<CompanyClosureNameFormDto> names = form.getNames();
        if (names == null || names.stream().noneMatch(name -> hasText(name.getName()))) {
            errors.rejectValue(ATTRIBUTE_NAMES, ERROR_NAMES_MANDATORY);
        }
    }

    /**
     * Validates the changes of an existing closure day: the names, the new version if one is entered, and the versions
     * starting in the current year or later, which may be changed. A version must not start at the same date as
     * another one, nor before the current year. Company-wide blackout periods only block an edit that closes more of
     * a day within them than is closed already.
     */
    void validateEdit(CompanyClosureEntry entry, CompanyClosureFormDto form, Errors errors) {
        final LocalDate today = LocalDate.now(clock);
        final LocalDate startOfYear = Year.now(clock).atDay(1);
        final boolean hasNewVersion = form.hasNewVersion(entry.dayLengthAt(today));

        validateNames(form, errors);
        if (hasNewVersion) {
            validateNewVersion(form, errors);
        }

        final Set<LocalDate> editableValidFroms = new HashSet<>();
        for (CompanyClosureVersion version : entry.versions()) {
            if (isEditable(version.validFrom(), startOfYear)) {
                editableValidFroms.add(version.validFrom());
            }
        }

        final List<CompanyClosureVersionFormDto> versions = form.getVersions();
        for (int index = 0; index < versions.size(); index++) {
            validateEditableVersion(versions.get(index), "versions[%d]".formatted(index), editableValidFroms, startOfYear, errors);
        }

        if (errors.hasErrors()) {
            return;
        }

        validateDistinctValidFroms(entry, form, hasNewVersion, errors);
        if (errors.hasErrors()) {
            return;
        }

        final List<CompanyClosureVersion> resultingVersions = resultingVersions(entry, form, hasNewVersion);
        final CompanyClosureEntry resultingEntry = new CompanyClosureEntry(entry.monthDay(), entry.nameByLocale(), entry.names(), resultingVersions);
        validateNoCompanyWideBlackoutPeriod(entry, resultingEntry, errors);
    }

    /**
     * @return whether a version with the given "valid from" may be changed: it starts in the current year or later
     */
    private static boolean isEditable(@Nullable LocalDate validFrom, LocalDate startOfYear) {
        return validFrom != null && !validFrom.isBefore(startOfYear);
    }

    private static void validateEditableVersion(CompanyClosureVersionFormDto version, String path, Set<LocalDate> editableValidFroms, LocalDate startOfYear, Errors errors) {
        // a version of the previous year after the turn of the year, or a manipulated form
        if (!editableValidFroms.contains(version.getOriginalValidFrom())) {
            errors.rejectValue(path + "." + ATTRIBUTE_VALID_FROM, ERROR_VERSION_NOT_EDITABLE);
            return;
        }
        if (version.getDayLength() == null) {
            errors.rejectValue(path + "." + ATTRIBUTE_DAY_LENGTH, ERROR_MANDATORY_FIELD);
        }
        if (version.getValidFrom() == null) {
            errors.rejectValue(path + "." + ATTRIBUTE_VALID_FROM, ERROR_MANDATORY_FIELD);
        } else if (version.getValidFrom().isBefore(startOfYear)) {
            errors.rejectValue(path + "." + ATTRIBUTE_VALID_FROM, ERROR_VALID_FROM_PREVIOUS_YEAR);
        }
    }

    /**
     * @return the stored versions with the changes of the form, as they are after saving: a version that is not part
     * of the form stays as it is. Includes the new version, sorted by "valid from" with the initial version first.
     */
    private static List<CompanyClosureVersion> resultingVersions(CompanyClosureEntry entry, CompanyClosureFormDto form, boolean hasNewVersion) {
        final Map<LocalDate, CompanyClosureVersion> changedByOldValidFrom = new HashMap<>();
        for (CompanyClosureVersionFormDto version : form.getVersions()) {
            changedByOldValidFrom.put(version.getOriginalValidFrom(), new CompanyClosureVersion(version.getDayLength(), version.getValidFrom()));
        }

        final List<CompanyClosureVersion> versions = new ArrayList<>();
        for (CompanyClosureVersion version : entry.versions()) {
            final CompanyClosureVersion changed = version.validFrom() == null ? null : changedByOldValidFrom.get(version.validFrom());
            versions.add(changed == null ? version : changed);
        }
        if (hasNewVersion) {
            versions.add(new CompanyClosureVersion(form.getDayLength(), form.getValidFrom()));
        }
        versions.sort(comparing(CompanyClosureVersion::validFrom, nullsFirst(naturalOrder())));
        return versions;
    }

    /**
     * The error is attached to the field that was entered: a version of the form, or the new version.
     */
    private static void validateDistinctValidFroms(CompanyClosureEntry entry, CompanyClosureFormDto form, boolean hasNewVersion, Errors errors) {
        final Set<LocalDate> submittedOriginals = new HashSet<>();
        for (CompanyClosureVersionFormDto version : form.getVersions()) {
            submittedOriginals.add(version.getOriginalValidFrom());
        }

        // the stored versions that are not part of the form keep their date
        final Set<LocalDate> seen = new HashSet<>();
        for (CompanyClosureVersion version : entry.versions()) {
            if (version.validFrom() != null && !submittedOriginals.contains(version.validFrom())) {
                seen.add(version.validFrom());
            }
        }

        final List<CompanyClosureVersionFormDto> versions = form.getVersions();
        for (int index = 0; index < versions.size(); index++) {
            if (!seen.add(versions.get(index).getValidFrom())) {
                errors.rejectValue("versions[%d].%s".formatted(index, ATTRIBUTE_VALID_FROM), ERROR_VALID_FROM_DUPLICATE);
            }
        }
        if (hasNewVersion && !seen.add(form.getValidFrom())) {
            errors.rejectValue(ATTRIBUTE_VALID_FROM, ERROR_VALID_FROM_DUPLICATE);
        }
    }

    private static boolean covers(DayLength dayLength, DayLength half) {
        return dayLength == FULL || dayLength == half;
    }

    /**
     * Validates a new closure day: the fields of {@link #validate(Object, Errors)} plus day and month.
     */
    void validateNew(CompanyClosureFormDto form, Errors errors) {

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
            validateNoCompanyWideBlackoutPeriod(monthDay, form.getDayLength(), form.getValidFrom(), errors);
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
     * An edit must not close more of a closure day within a company-wide blackout period than is closed already. Each
     * day is compared before and after the edit, since a changed or moved version also changes the days the versions
     * next to it apply to, e.g. a later "valid from" extends the version before it.
     */
    private void validateNoCompanyWideBlackoutPeriod(CompanyClosureEntry stored, CompanyClosureEntry resulting, Errors errors) {
        blackoutPeriodService.getAllBlackoutPeriods().stream()
            .filter(BlackoutPeriod::isCompanyWide)
            .sorted(comparing(BlackoutPeriod::getStartDate))
            .filter(period -> closesMoreIn(period, stored, resulting))
            .findFirst()
            .ifPresent(period -> errors.reject(ERROR_BLACKOUT_PERIOD, new Object[]{period.getTitle()}, null));
    }

    private static boolean closesMoreIn(BlackoutPeriod period, CompanyClosureEntry stored, CompanyClosureEntry resulting) {
        return closureDaysIn(period, stored.monthDay())
            .anyMatch(date -> closesMoreAt(date, stored, resulting));
    }

    private static boolean closesMoreAt(LocalDate date, CompanyClosureEntry stored, CompanyClosureEntry resulting) {
        final DayLength before = stored.dayLengthAt(date);
        final DayLength after = resulting.dayLengthAt(date);
        return (covers(after, MORNING) && !covers(before, MORNING)) || (covers(after, NOON) && !covers(before, NOON));
    }

    /**
     * A new closure day recurs every year from "valid from", so a company-wide blackout period in any of these years
     * containing the day blocks it. Removing the closure ({@code ZERO}) is never blocked.
     */
    private void validateNoCompanyWideBlackoutPeriod(MonthDay monthDay, @Nullable DayLength dayLength, @Nullable LocalDate validFrom, Errors errors) {
        if (dayLength == null || dayLength == ZERO || validFrom == null) {
            return;
        }

        blackoutPeriodService.getAllBlackoutPeriods().stream()
            .filter(BlackoutPeriod::isCompanyWide)
            .sorted(comparing(BlackoutPeriod::getStartDate))
            .filter(period -> containsClosureDayFrom(period, monthDay, validFrom))
            .findFirst()
            .ifPresent(period -> errors.reject(ERROR_BLACKOUT_PERIOD, new Object[]{period.getTitle()}, null));
    }

    private static boolean containsClosureDayFrom(BlackoutPeriod period, MonthDay monthDay, LocalDate validFrom) {
        return closureDaysIn(period, monthDay).anyMatch(date -> !date.isBefore(validFrom));
    }

    /**
     * @return the dates of the closure day within the period
     */
    private static Stream<LocalDate> closureDaysIn(BlackoutPeriod period, MonthDay monthDay) {
        return rangeClosed(period.getStartDate().getYear(), period.getEndDate().getYear())
            .filter(monthDay::isValidYear)
            .mapToObj(monthDay::atYear)
            .filter(date -> isInPeriod(date, period));
    }

    private static boolean isInPeriod(LocalDate date, BlackoutPeriod period) {
        return !date.isBefore(period.getStartDate()) && !date.isAfter(period.getEndDate());
    }
}
