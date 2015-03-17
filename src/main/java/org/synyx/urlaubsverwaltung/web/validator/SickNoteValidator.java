package org.synyx.urlaubsverwaltung.web.validator;

import org.joda.time.DateMidnight;
import org.joda.time.Interval;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.application.domain.OverlapCase;
import org.synyx.urlaubsverwaltung.core.application.service.OverlapService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteComment;


/**
 * Class for validating {@link SickNote} object.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Component
public class SickNoteValidator implements Validator {

    private static final String MANDATORY_FIELD = "error.mandatory.field";
    private static final String ERROR_PERIOD = "error.period";
    private static final String ERROR_PERIOD_SICK_NOTE = "error.period.sicknote";
    private static final String ERROR_LENGTH = "error.length";
    private static final String ERROR_OVERLAP = "error.overlap";

    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";
    private static final String AUB_START_DATE = "aubStartDate";
    private static final String AUB_END_DATE = "aubEndDate";
    private static final String COMMENT = "text";

    private static final int MAX_LENGTH = 200;

    private final OverlapService overlapService;

    @Autowired
    public SickNoteValidator(OverlapService overlapService) {

        this.overlapService = overlapService;
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

        DateMidnight startDate = sickNote.getStartDate();
        DateMidnight endDate = sickNote.getEndDate();

        validateNotNull(startDate, START_DATE, errors);
        validateNotNull(endDate, END_DATE, errors);

        if (startDate != null && endDate != null) {
            validatePeriod(startDate, endDate, END_DATE, errors);
            validateNoOverlapping(sickNote, errors);
        }
    }


    private void validateAUPeriod(SickNote sickNote, Errors errors) {

        DateMidnight aubStartDate = sickNote.getAubStartDate();
        DateMidnight aubEndDate = sickNote.getAubEndDate();

        validateNotNull(aubStartDate, AUB_START_DATE, errors);
        validateNotNull(aubEndDate, AUB_END_DATE, errors);

        if (aubStartDate != null && aubEndDate != null) {
            validatePeriod(aubStartDate, aubEndDate, AUB_END_DATE, errors);

            // Intervals are inclusive of the start instant and exclusive of the end, i.e. add one day at the end
            Interval interval = new Interval(sickNote.getStartDate(), sickNote.getEndDate().plusDays(1));

            if (!interval.contains(aubStartDate)) {
                errors.rejectValue(AUB_START_DATE, ERROR_PERIOD_SICK_NOTE);
            }

            if (!interval.contains(aubEndDate)) {
                errors.rejectValue(AUB_END_DATE, ERROR_PERIOD_SICK_NOTE);
            }
        }
    }


    private void validateNotNull(DateMidnight date, String field, Errors errors) {

        if (date == null) {
            // may be that date field is null because of cast exception, than there is already a field error
            if (errors.getFieldErrors(field).isEmpty()) {
                errors.rejectValue(field, MANDATORY_FIELD);
            }
        }
    }


    /**
     * Validate that the given start date is not after the given end date.
     *
     * @param  startDate
     * @param  endDate
     * @param  field
     * @param  errors
     */
    private void validatePeriod(DateMidnight startDate, DateMidnight endDate, String field, Errors errors) {

        if (startDate.isAfter(endDate)) {
            errors.rejectValue(field, ERROR_PERIOD);
        }
    }


    private void validateNoOverlapping(SickNote sickNote, Errors errors) {

        /**
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
            if (text.length() > MAX_LENGTH) {
                errors.rejectValue(COMMENT, ERROR_LENGTH);
            }
        } else {
            errors.rejectValue(COMMENT, MANDATORY_FIELD);
        }
    }
}
