package org.synyx.urlaubsverwaltung.validator;

import org.joda.time.DateMidnight;
import org.joda.time.Interval;

import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteComment;


/**
 * Class for validating {@link SickNote} object.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SickNoteValidator implements Validator {

    private static final String MANDATORY_FIELD = "error.mandatory.field";
    private static final String ERROR_PERIOD = "error.period";
    private static final String ERROR_PERIOD_SICKNOTE = "error.period.sicknote";
    private static final String ERROR_LENGTH = "error.length";

    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";
    private static final String AUB_START_DATE = "aubStartDate";
    private static final String AUB_END_DATE = "aubEndDate";
    private static final String COMMENT = "text";

    private static final int MAX_LENGTH = 200;

    @Override
    public boolean supports(Class<?> clazz) {

        return SickNote.class.equals(clazz);
    }


    @Override
    public void validate(Object target, Errors errors) {

        SickNote sickNote = (SickNote) target;

        DateMidnight startDate = sickNote.getStartDate();
        DateMidnight endDate = sickNote.getEndDate();

        validateNotNull(startDate, START_DATE, errors);
        validateNotNull(endDate, END_DATE, errors);

        if (startDate != null && endDate != null) {
            validatePeriod(startDate, endDate, END_DATE, errors);
        }

        startDate = sickNote.getAubStartDate();
        endDate = sickNote.getAubEndDate();

        // not valid if one field is null and the other not
        if ((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
            errors.rejectValue(AUB_END_DATE, ERROR_PERIOD);
        }

        if (sickNote.isAubPresent()) {
            // if the AU is present, there must be a period given
            if (startDate != null && endDate != null) {
                validatePeriod(startDate, endDate, AUB_END_DATE, errors);
                validateAUPeriod(sickNote, errors);
            } else {
                validateNotNull(startDate, AUB_START_DATE, errors);
                validateNotNull(endDate, AUB_END_DATE, errors);
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


    private void validateAUPeriod(SickNote sickNote, Errors errors) {

        // Intervals are inclusive of the start instant and exclusive of the end, i.e. add one day at the end
        Interval interval = new Interval(sickNote.getStartDate(), sickNote.getEndDate().plusDays(1));

        if (!interval.contains(sickNote.getAubStartDate())) {
            errors.rejectValue(AUB_START_DATE, ERROR_PERIOD_SICKNOTE);
        }

        if (!interval.contains(sickNote.getAubEndDate())) {
            errors.rejectValue(AUB_END_DATE, ERROR_PERIOD_SICKNOTE);
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
