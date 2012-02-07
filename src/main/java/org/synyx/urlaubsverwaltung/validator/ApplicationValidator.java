/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.validator;

import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.domain.Comment;
import org.synyx.urlaubsverwaltung.domain.DayLength;
import org.synyx.urlaubsverwaltung.domain.VacationType;
import org.synyx.urlaubsverwaltung.view.AppForm;

import java.math.BigDecimal;


/**
 * This class validate if an application's form ('AppForm') is filled correctly by the user, else it saves error
 * messages in errors object.
 *
 * @author  Aljona Murygina
 */
public class ApplicationValidator implements Validator {

    // errors' properties keys
    private static final String MANDATORY_FIELD = "error.mandatory.field";
    private static final String ERROR_ENTRY = "error.entry";
    private static final String ERROR_REASON = "error.reason";
    private static final String ERROR_PERIOD = "error.period";
    private static final String ERROR_PAST = "error.period.past";
    private static final String ERROR_SICK = "sick.more";

    // names of fields
    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";
    private static final String START_DATE_HALF = "startDateHalf";
    private static final String REASON = "reason";
    private static final String TEXT = "text";
    private static final String SICK_DAYS = "sickDays";

    @Override
    public boolean supports(Class<?> clazz) {

        return AppForm.class.equals(clazz);
    }


    @Override
    public void validate(Object target, Errors errors) {

        AppForm app = (AppForm) target;

        // check if date fields are valid
        if (app.getHowLong() == DayLength.FULL) {
            // check if date fields are not filled
            if (app.getStartDate() == null) {
                if (errors.getFieldErrors(START_DATE).isEmpty()) {
                    errors.rejectValue(START_DATE, MANDATORY_FIELD);
                }
            }

            if (app.getEndDate() == null) {
                if (errors.getFieldErrors(END_DATE).isEmpty()) {
                    errors.rejectValue(END_DATE, MANDATORY_FIELD);
                }
            }

            if (app.getStartDate() != null && app.getEndDate() != null) {
                // check if from < to
                if (app.getStartDate().isAfter(app.getEndDate())) {
                    errors.reject(ERROR_PERIOD);
                }
            }
        } else {
            if (app.getStartDateHalf() == null) {
                if (errors.getFieldErrors(START_DATE_HALF).isEmpty()) {
                    errors.rejectValue(START_DATE_HALF, MANDATORY_FIELD);
                }
            }
        }

        // check if reason is not filled
        if (app.getVacationType() != VacationType.HOLIDAY) {
            if (app.getReason() == null || !StringUtils.hasText(app.getReason())) {
                errors.rejectValue(REASON, MANDATORY_FIELD);
            }
        }
    }


    /**
     * If a user (or a boss) applies for leave, startDate has to be equal or greater than now(). The office is able to
     * apply for leave for someone else and may apply for past too.
     *
     * @param  target
     * @param  errors
     */
    public void validateForUser(Object target, Errors errors) {

        AppForm app = (AppForm) target;

        // check if given dates are valid
        if (app.getHowLong() == DayLength.FULL) {
            if (app.getStartDate() != null) {
                if (app.getStartDate().isBeforeNow()) {
                    errors.reject(ERROR_PAST);
                }
            }
        } else {
            if (app.getStartDateHalf() != null) {
                if (app.getStartDateHalf().isBeforeNow()) {
                    errors.reject(ERROR_PAST);
                }
            }
        }
    }


    public void validateSickDays(Object target, BigDecimal days, Errors errors) {

        AppForm app = (AppForm) target;

        // field not filled
        if (app.getSickDays() == null) {
            if (errors.getFieldErrors(SICK_DAYS).isEmpty()) {
                errors.rejectValue(SICK_DAYS, "sick.empty");
            }
        } else {
            // number of sick days is zero or negative
            if (app.getSickDays().compareTo(BigDecimal.ZERO) == 0) {
                errors.rejectValue(SICK_DAYS, "sick.zero");
            }

            if (app.getSickDays().compareTo(BigDecimal.ZERO) == -1) {
                errors.rejectValue(SICK_DAYS, "sick.negative");
            }

            // number of sick days is greater than vacation days of application
            if (app.getSickDays().compareTo(days) == 1)
                errors.rejectValue(SICK_DAYS, ERROR_SICK);
        }
    }


    public void validateComment(Object target, Errors errors) {

        Comment comment = (Comment) target;

        if (comment.getText() == null || !StringUtils.hasText(comment.getText())) {
            errors.rejectValue(TEXT, ERROR_REASON);
        }
    }
}
