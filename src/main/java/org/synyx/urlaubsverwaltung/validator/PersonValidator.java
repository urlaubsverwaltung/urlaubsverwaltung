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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class validate if an person's form ('PersonForm') is filled correctly by the user, else it saves error messages
 * in errors object.
 *
 * @author  Aljona Murygina
 */
public class PersonValidator implements Validator {

    private static final String MANDATORY_FIELD = "error.mandatory.field";
    private static final String ERROR_ENTRY = "error.entry";
    private static final String ERROR_EMAIL = "error.email";

    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String VACATION_DAYS = "vacationDays";
    private static final String REMAINING_VACATION_DAYS = "remainingVacationDays";
    private static final String YEAR = "year";
    private static final String EMAIL = "email";

    private static final String EMAIL_PATTERN =
        "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final String NAME_PATTERN = "\\p{L}+"; // any kind of letter from any language.

    private Pattern pattern;
    private Matcher matcher;

    @Override
    public boolean supports(Class<?> clazz) {

        return PersonForm.class.equals(clazz);
    }


    @Override
    public void validate(Object target, Errors errors) {

        PersonForm form = (PersonForm) target;

        // field first name
        validateName(form.getFirstName(), FIRST_NAME, errors);

        // field last name
        validateName(form.getLastName(), LAST_NAME, errors);

        // field email address
        validateEmail(form.getEmail(), errors);

        // field year
        validateYear(form.getYear(), errors);

        // field vacation days
        validateNumberOfDays(form.getVacationDays(), VACATION_DAYS, 40, errors);

        // field remaining vacation days
        validateNumberOfDays(form.getRemainingVacationDays(), REMAINING_VACATION_DAYS, 20, errors);
    }


    private boolean matchPattern(String nameOfPattern, String matchSequence) {

        pattern = Pattern.compile(nameOfPattern);
        matcher = pattern.matcher(matchSequence);

        return matcher.matches();
    }


    protected void validateName(String name, String field, Errors errors) {

        // is the name field null/empty?
        if (name == null || !StringUtils.hasText(name)) {
            errors.rejectValue(field, MANDATORY_FIELD);
        } else {
            // contains the name field digits?
            if (!matchPattern(NAME_PATTERN, name)) {
                errors.rejectValue(field, ERROR_ENTRY);
            }
        }
    }


    protected void validateEmail(String email, Errors errors) {

        // is email field null or empty
        if (email == null || !StringUtils.hasText(email)) {
            errors.rejectValue(EMAIL, MANDATORY_FIELD);
        } else {
            // validation with regex
            if (!matchPattern(EMAIL_PATTERN, email)) {
                errors.rejectValue(EMAIL, ERROR_EMAIL);
            }
        }
    }


    protected void validateYear(String yearForm, Errors errors) {

        // is year field filled?
        if (yearForm == null || !StringUtils.hasText(yearForm)) {
            errors.rejectValue(YEAR, MANDATORY_FIELD);
        } else {
            try {
                int year = Integer.parseInt(yearForm);

                if (year < 2010 || year > 2030) {
                    errors.rejectValue(YEAR, ERROR_ENTRY);
                }
            } catch (NumberFormatException ex) {
                errors.rejectValue(YEAR, ERROR_ENTRY);
            }
        }
    }


    protected void validateNumberOfDays(BigDecimal days, String field, double maximumDays, Errors errors) {

        // is field filled?
        if (days == null) {
            if (errors.getFieldErrors(VACATION_DAYS).isEmpty()) {
                errors.rejectValue(field, MANDATORY_FIELD);
            }
        } else {
            // is number of days < 0 ?
            if (days.compareTo(BigDecimal.ZERO) == -1) {
                errors.rejectValue(field, ERROR_ENTRY);
            }

            // is number of days unrealistic?
            if (days.compareTo(BigDecimal.valueOf(maximumDays)) == 1) {
                errors.rejectValue(field, ERROR_ENTRY);
            }
        }
    }
}
