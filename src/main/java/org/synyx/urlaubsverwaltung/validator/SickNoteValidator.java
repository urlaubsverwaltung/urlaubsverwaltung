package org.synyx.urlaubsverwaltung.validator;

import org.joda.time.DateMidnight;

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
    private static final String ERROR_LENGTH = "error.length";

    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";
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

        if (startDate == null) {
            if (errors.getFieldErrors(START_DATE).isEmpty()) {
                errors.rejectValue(START_DATE, MANDATORY_FIELD);
            }
        }

        if (endDate == null) {
            if (errors.getFieldErrors(END_DATE).isEmpty()) {
                errors.rejectValue(END_DATE, MANDATORY_FIELD);
            }
        }

        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                errors.rejectValue(END_DATE, ERROR_PERIOD);
            }
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
