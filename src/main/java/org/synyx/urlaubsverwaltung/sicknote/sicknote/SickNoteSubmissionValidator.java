package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.overlap.OverlapCase;
import org.synyx.urlaubsverwaltung.overlap.OverlapService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.overlap.OverlapCase.FULLY_OVERLAPPING;
import static org.synyx.urlaubsverwaltung.overlap.OverlapCase.PARTLY_OVERLAPPING;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;

/**
 * Class for validating {@link SickNote} object.
 */
@Component
public class SickNoteSubmissionValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_PERIOD = "error.entry.invalidPeriod";
    private static final String ERROR_PERIOD_SICK_NOTE = "sicknote.error.aubInvalidPeriod";
    private static final String ERROR_HALF_DAY_PERIOD_SICK_NOTE = "sicknote.error.halfDayPeriod";
    private static final String ERROR_OVERLAP = "application.error.overlap";
    private static final String ERROR_WORKING_TIME = "sicknote.error.noValidWorkingTime";
    private static final String ERROR_ROLES = "sicknote.error.noValidPermissionToApplySickNote";

    private static final String ATTRIBUTE_DAY_LENGTH = "dayLength";
    private static final String ATTRIBUTE_START_DATE = "startDate";
    private static final String ATTRIBUTE_END_DATE = "endDate";
    private static final String ATTRIBUTE_AUB_START_DATE = "aubStartDate";
    private static final String ATTRIBUTE_AUB_END_DATE = "aubEndDate";

    private final OverlapService overlapService;
    private final WorkingTimeService workingTimeService;

    private final SettingsService settingsService;
    private final Clock clock;

    @Autowired
    public SickNoteSubmissionValidator(OverlapService overlapService, WorkingTimeService workingTimeService, SettingsService settingsService, Clock clock) {
        this.overlapService = overlapService;
        this.workingTimeService = workingTimeService;
        this.settingsService = settingsService;
        this.clock = clock;
    }

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return SickNote.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {

        final SickNote sickNote = (SickNote) target;
        validateApplier(sickNote, errors);
        validateSickNotePeriod(sickNote, errors);

        if (!errors.hasErrors() && sickNote.isAubPresent()) {
            validateAUPeriod(sickNote, errors);
        }
    }

    private void validateApplier(SickNote sickNote, Errors errors) {
        final Person applier = sickNote.getApplier();
        if (applier == null) {
            return;
        }

        if (!applier.equals(sickNote.getPerson()) || !settingsService.getSettings().getSickNoteSettings().getUserIsAllowedToSubmitSickNotes()) {
            errors.reject(ERROR_ROLES);
        }
    }

    private void validateSickNotePeriod(SickNote sickNote, Errors errors) {

        final DayLength dayLength = sickNote.getDayLength();
        final LocalDate startDate = sickNote.getStartDate();
        final LocalDate endDate = sickNote.getEndDate();

        validateNotNull(startDate, ATTRIBUTE_START_DATE, errors);
        validateNotNull(endDate, ATTRIBUTE_END_DATE, errors);

        if (dayLength == null) {
            errors.rejectValue(ATTRIBUTE_DAY_LENGTH, ERROR_MANDATORY_FIELD);
        }

        if (dayLength != null && startDate != null && endDate != null) {
            validatePeriod(startDate, endDate, dayLength, ATTRIBUTE_END_DATE, errors);

            if (!errors.hasErrors()) {
                validateNoOverlapping(sickNote, errors);
            }
        }
    }

    private void validateAUPeriod(SickNote sickNote, Errors errors) {

        final DayLength dayLength = sickNote.getDayLength();
        final LocalDate aubStartDate = sickNote.getAubStartDate();
        final LocalDate aubEndDate = sickNote.getAubEndDate();

        validateNotNull(aubStartDate, ATTRIBUTE_AUB_START_DATE, errors);
        validateNotNull(aubEndDate, ATTRIBUTE_AUB_END_DATE, errors);

        if (aubStartDate != null && aubEndDate != null) {
            validatePeriod(aubStartDate, aubEndDate, dayLength, ATTRIBUTE_AUB_END_DATE, errors);

            final LocalDate sickNoteStartDate = sickNote.getStartDate();
            final LocalDate sickNoteEndDate = sickNote.getEndDate();

            if (sickNoteStartDate != null && sickNoteEndDate != null) {
                // Intervals are inclusive of the start instant and exclusive of the end, i.e. add one day at the end
                final long start = sickNoteStartDate.atStartOfDay(clock.getZone()).toInstant().toEpochMilli();
                final long end = sickNoteEndDate.plusDays(1).atStartOfDay(clock.getZone()).toInstant().toEpochMilli();
                final Interval sickNoteInterval = new Interval(start, end);

                if (!sickNoteInterval.contains(aubStartDate.atStartOfDay(clock.getZone()).toInstant().toEpochMilli())) {
                    errors.rejectValue(ATTRIBUTE_AUB_START_DATE, ERROR_PERIOD_SICK_NOTE);
                }

                if (!sickNoteInterval.contains(aubEndDate.atStartOfDay(clock.getZone()).toInstant().toEpochMilli())) {
                    errors.rejectValue(ATTRIBUTE_AUB_END_DATE, ERROR_PERIOD_SICK_NOTE);
                }
            }
        }
    }

    private void validateNotNull(LocalDate date, String field, Errors errors) {

        // may be that date field is null because of cast exception, than there is already a field error
        if (date == null && errors.getFieldErrors(field).isEmpty()) {
            errors.rejectValue(field, ERROR_MANDATORY_FIELD);
        }
    }

    /**
     * Validate that the given start date is not after the given end date.
     */
    private void validatePeriod(LocalDate startDate, LocalDate endDate, DayLength dayLength, String field, Errors errors) {

        if (startDate.isAfter(endDate)) {
            errors.rejectValue(field, ERROR_PERIOD);
        } else {
            final boolean isHalfDay = dayLength == MORNING || dayLength == NOON;
            if (isHalfDay && !startDate.isEqual(endDate)) {
                errors.rejectValue(field, ERROR_HALF_DAY_PERIOD_SICK_NOTE);
            }
        }
    }

    private void validateNoOverlapping(SickNote sickNote, Errors errors) {

        // Ensure the person has a working time for the period of the sick note
        final Optional<WorkingTime> maybeWorkingTime = workingTimeService.getWorkingTime(sickNote.getPerson(), sickNote.getStartDate());
        if (maybeWorkingTime.isEmpty()) {
            errors.reject(ERROR_WORKING_TIME);
            return;
        }

        /*
         * Ensure that there is no application for leave and no sick note in the same period
         */
        final OverlapCase overlap = overlapService.checkOverlap(sickNote);
        final boolean isOverlapping = overlap == FULLY_OVERLAPPING || overlap == PARTLY_OVERLAPPING;

        if (isOverlapping) {
            errors.reject(ERROR_OVERLAP);
        }
    }
}
