/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.validator;

import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.view.PersonForm;


/**
 * This class validate if an person's form ('PersonForm') is filled correctly by the user, else it saves error messages
 * in errors object.
 *
 * @author  Aljona Murygina
 */
public class PersonValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {

        return PersonForm.class.equals(clazz);
    }


    @Override
    public void validate(Object target, Errors errors) {

        PersonForm form = (PersonForm) target;

        // are the name fields null/empty?

        if (form.getFirstName() == null || !StringUtils.hasText(form.getFirstName())) {
            errors.rejectValue("firstName", "error.mandatory.field");
        }

        if (form.getLastName() == null || !StringUtils.hasText(form.getLastName())) {
            errors.rejectValue("lastName", "error.mandatory.field");
        }

        // if email field is filled, is this a valid email address?

        if (StringUtils.hasText(form.getEmail())) {
            if (!form.getEmail().contains("@")) {
                errors.rejectValue("email", "error.entry");
            }
        }

        // is year field filled?
        if (!StringUtils.hasText(form.getYear())) {
            errors.rejectValue("year", "error.mandatory.field");
        }

        // if year field is filled, it has to be a number resp. a valid year (> 2010)
        if (StringUtils.hasText(form.getYear())) {
            try {
                int year = Integer.parseInt(form.getYear());

                if (year < 2010 || year > 2030) {
                    errors.rejectValue("year", "error.entry");
                }
            } catch (NumberFormatException ex) {
                errors.rejectValue("year", "error.entry");
            }
        }
    }
}
