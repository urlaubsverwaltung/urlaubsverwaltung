/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.validator;

import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.view.PersonForm;

import java.math.BigDecimal;


/**
 * This class validate if an person's form ('PersonForm') is filled correctly by the user, else it saves error messages
 * in errors object.
 *
 * @author  Aljona Murygina
 */
public class PersonValidator implements Validator {

    private static final String MANDATORY_FIELD = "error.mandatory.field";
    private static final String ERROR_ENTRY = "error.entry";

    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String VACATION_DAYS = "vacationDays";
    private static final String REMAINING_VACATION_DAYS = "remainingVacationDays";
    private static final String YEAR = "year";
    private static final String EMAIL = "email";

    @Override
    public boolean supports(Class<?> clazz) {

        return PersonForm.class.equals(clazz);
    }


    @Override
    public void validate(Object target, Errors errors) {

        PersonForm form = (PersonForm) target;

        // are the name fields null/empty?

        if (form.getFirstName() == null || !StringUtils.hasText(form.getFirstName())) {
            errors.rejectValue(FIRST_NAME, MANDATORY_FIELD);
        }

        if (form.getLastName() == null || !StringUtils.hasText(form.getLastName())) {
            errors.rejectValue(LAST_NAME, MANDATORY_FIELD);
        }

        // email field filled?
        if (form.getEmail() == null || !StringUtils.hasText(form.getEmail())) {
            errors.rejectValue(EMAIL, MANDATORY_FIELD);
        }

        // if email field is filled, is this a valid email address?

        if (StringUtils.hasText(form.getEmail())) {
            if (!form.getEmail().contains("@")) {
                errors.rejectValue(EMAIL, ERROR_ENTRY);
            }
        }

        // is year field filled?
        if (!StringUtils.hasText(form.getYear())) {
            errors.rejectValue(YEAR, MANDATORY_FIELD);
        }

        // if year field is filled, it has to be a number resp. a valid year (> 2010)
        if (StringUtils.hasText(form.getYear())) {
            try {
                int year = Integer.parseInt(form.getYear());

                if (year < 2010 || year > 2030) {
                    errors.rejectValue(YEAR, ERROR_ENTRY);
                }
            } catch (NumberFormatException ex) {
                errors.rejectValue(YEAR, ERROR_ENTRY);
            }
        }

        // if vacation days fields are not filled
        if (form.getVacationDays() == null) {
            errors.rejectValue(VACATION_DAYS, MANDATORY_FIELD);
        }

        if (form.getRemainingVacationDays() == null) {
            errors.rejectValue(REMAINING_VACATION_DAYS, MANDATORY_FIELD);
        }

        if (form.getVacationDays() != null) {
            // if the given number of days < 0
            if (form.getVacationDays().compareTo(BigDecimal.ZERO) == -1) {
                errors.rejectValue(VACATION_DAYS, ERROR_ENTRY);
            }

            // if the given number of days is unrealistic
            if (form.getVacationDays().compareTo(BigDecimal.valueOf(40)) == 1) {
                errors.rejectValue(VACATION_DAYS, ERROR_ENTRY);
            }
        }

        if (form.getRemainingVacationDays() != null) {
            if (form.getRemainingVacationDays().compareTo(BigDecimal.ZERO) == -1) {
                errors.rejectValue(REMAINING_VACATION_DAYS, ERROR_ENTRY);
            }

            if (form.getRemainingVacationDays().compareTo(BigDecimal.valueOf(20)) == 1) {
                errors.rejectValue(REMAINING_VACATION_DAYS, ERROR_ENTRY);
            }
        }
    }
}
