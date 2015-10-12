package org.synyx.urlaubsverwaltung.web.overtime;

import org.joda.time.DateMidnight;

import org.springframework.stereotype.Component;

import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.util.CalcUtil;

import java.math.BigDecimal;


/**
 * Validates overtime record.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Component
public class OvertimeValidator implements Validator {

    private static final int MAX_CHARS = 200;

    private static final String ERROR_MANDATORY = "error.entry.mandatory";
    private static final String ERROR_MAX_CHARS = "error.entry.tooManyChars";
    private static final String ERROR_INVALID_PERIOD = "error.entry.invalidPeriod";
    private static final String ERROR_NUMBER_OF_HOURS = "overtime.data.numberOfHours.error";

    @Override
    public boolean supports(Class<?> clazz) {

        return OvertimeForm.class.equals(clazz);
    }


    @Override
    public void validate(Object target, Errors errors) {

        OvertimeForm overtimeForm = (OvertimeForm) target;

        validatePeriod(overtimeForm, errors);
        validateNumberOfHours(overtimeForm, errors);
        validateComment(overtimeForm, errors);
    }


    private void validatePeriod(OvertimeForm overtimeForm, Errors errors) {

        DateMidnight startDate = overtimeForm.getStartDate();
        DateMidnight endDate = overtimeForm.getEndDate();

        if (startDate == null) {
            errors.rejectValue("startDate", ERROR_MANDATORY);
        }

        if (endDate == null) {
            errors.rejectValue("endDate", ERROR_MANDATORY);
        }

        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            errors.rejectValue("endDate", ERROR_INVALID_PERIOD);
        }
    }


    private void validateNumberOfHours(OvertimeForm overtimeForm, Errors errors) {

        BigDecimal numberOfHours = overtimeForm.getNumberOfHours();

        if (numberOfHours == null) {
            errors.rejectValue("numberOfHours", ERROR_MANDATORY);

            return;
        }

        if (!CalcUtil.isPositive(numberOfHours)) {
            errors.rejectValue("numberOfHours", ERROR_NUMBER_OF_HOURS);
        }
    }


    private void validateComment(OvertimeForm overtimeForm, Errors errors) {

        String comment = overtimeForm.getComment();

        if (StringUtils.hasText(comment) && comment.length() > MAX_CHARS) {
            errors.rejectValue("comment", ERROR_MAX_CHARS);
        }
    }
}
