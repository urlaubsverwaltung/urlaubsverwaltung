package org.synyx.urlaubsverwaltung.sicknote.web;

import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteComment;
import org.synyx.urlaubsverwaltung.workingtime.OverlapCase;
import org.synyx.urlaubsverwaltung.workingtime.OverlapService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;


/**
 * Class for validating {@link SickNote} object.
 */
@Component
public class SickNoteValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_PERIOD = "error.entry.invalidPeriod";
    private static final String ERROR_LENGTH = "error.entry.tooManyChars";
    private static final String ERROR_PERIOD_SICK_NOTE = "sicknote.error.aubInvalidPeriod";
    private static final String ERROR_HALF_DAY_PERIOD_SICK_NOTE = "sicknote.error.halfDayPeriod";
    private static final String ERROR_OVERLAP = "application.error.overlap";
    private static final String ERROR_WORKING_TIME = "sicknote.error.noValidWorkingTime";

    private static final String ATTRIBUTE_DAY_LENGTH = "dayLength";
    private static final String ATTRIBUTE_START_DATE = "startDate";
    private static final String ATTRIBUTE_END_DATE = "endDate";
    private static final String ATTRIBUTE_AUB_START_DATE = "aubStartDate";
    private static final String ATTRIBUTE_AUB_END_DATE = "aubEndDate";
    private static final String ATTRIBUTE_COMMENT = "text";

    private static final int MAX_CHARS = 200;

    private final OverlapService overlapService;
    private final WorkingTimeService workingTimeService;

    @Autowired
    public SickNoteValidator(OverlapService overlapService, WorkingTimeService workingTimeService) {

        this.overlapService = overlapService;
        this.workingTimeService = workingTimeService;
    }

    @Override
    public boolean supports(Class<?> clazz) {

        return SickNote.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        SickNote sickNote = (SickNote) target;

        validateSickNotePeriod(sickNote, errors);

        if (sickNote.isAubPresent()) {
            validateAUPeriod(sickNote, errors);
        }
    }

    private void validateSickNotePeriod(SickNote sickNote, Errors errors) {

        DayLength dayLength = sickNote.getDayLength();
        LocalDate startDate = sickNote.getStartDate();
        LocalDate endDate = sickNote.getEndDate();

        validateNotNull(startDate, ATTRIBUTE_START_DATE, errors);
        validateNotNull(endDate, ATTRIBUTE_END_DATE, errors);

        if (dayLength == null) {
            errors.rejectValue(ATTRIBUTE_DAY_LENGTH, ERROR_MANDATORY_FIELD);
        }

        if (dayLength != null && startDate != null && endDate != null) {
            validatePeriod(startDate, endDate, dayLength, ATTRIBUTE_END_DATE, errors);
            validateNoOverlapping(sickNote, errors);
        }
    }

    private void validateAUPeriod(SickNote sickNote, Errors errors) {

        DayLength dayLength = sickNote.getDayLength();
        LocalDate aubStartDate = sickNote.getAubStartDate();
        LocalDate aubEndDate = sickNote.getAubEndDate();

        validateNotNull(aubStartDate, ATTRIBUTE_AUB_START_DATE, errors);
        validateNotNull(aubEndDate, ATTRIBUTE_AUB_END_DATE, errors);

        if (aubStartDate != null && aubEndDate != null) {
            validatePeriod(aubStartDate, aubEndDate, dayLength, ATTRIBUTE_AUB_END_DATE, errors);

            LocalDate startDate = sickNote.getStartDate();
            LocalDate endDate = sickNote.getEndDate();

            if (startDate != null && endDate != null && endDate.isAfter(startDate)) {
                // Intervals are inclusive of the start instant and exclusive of the end, i.e. add one day at the end
                Interval interval = new Interval(startDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
                    endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli());

                if (!interval.contains(aubStartDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())) {
                    errors.rejectValue(ATTRIBUTE_AUB_START_DATE, ERROR_PERIOD_SICK_NOTE);
                }

                if (!interval.contains(aubEndDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())) {
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
    private void validatePeriod(LocalDate startDate, LocalDate endDate, DayLength dayLength, String field,
                                Errors errors) {

        if (startDate.isAfter(endDate)) {
            errors.rejectValue(field, ERROR_PERIOD);
        } else {
            boolean isHalfDay = dayLength == DayLength.MORNING || dayLength == DayLength.NOON;

            if (isHalfDay && !startDate.isEqual(endDate)) {
                errors.rejectValue(field, ERROR_HALF_DAY_PERIOD_SICK_NOTE);
            }
        }
    }

    private void validateNoOverlapping(SickNote sickNote, Errors errors) {

        /*
         * Ensure the person has a working time for the period of the sick note
         */
        Optional<WorkingTime> workingTime = workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(
            sickNote.getPerson(), sickNote.getStartDate());

        if (workingTime.isEmpty()) {
            errors.reject(ERROR_WORKING_TIME);

            return;
        }

        /*
         * Ensure that there is no application for leave and no sick note in the same period
         */
        OverlapCase overlap = overlapService.checkOverlap(sickNote);

        boolean isOverlapping = overlap == OverlapCase.FULLY_OVERLAPPING || overlap == OverlapCase.PARTLY_OVERLAPPING;

        if (isOverlapping) {
            errors.reject(ERROR_OVERLAP);
        }
    }

    public void validateComment(SickNoteComment comment, Errors errors) {

        String text = comment.getText();

        if (StringUtils.hasText(text)) {
            if (text.length() > MAX_CHARS) {
                errors.rejectValue(ATTRIBUTE_COMMENT, ERROR_LENGTH);
            }
        } else {
            errors.rejectValue(ATTRIBUTE_COMMENT, ERROR_MANDATORY_FIELD);
        }
    }
}
