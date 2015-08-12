package org.synyx.urlaubsverwaltung.web.validator;

import org.springframework.stereotype.Component;

import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.department.Department;


/**
 * Validates the content of {@link Department}s.
 *
 * @author  Daniel Hammann - <hammann@synyx.de>
 */
@Component
public class DepartmentValidator implements Validator {

    private static final int MAX_CHARS_NAME = 50;
    private static final int MAX_CHARS_DESC = 200;

    private static final String FIELD_NAME = "name";
    private static final String FIELD_DESCRIPTION = "description";

    private static final String ERROR_REASON = "error.entry.mandatory";
    private static final String ERROR_LENGTH = "error.entry.tooManyChars";

    @Override
    public boolean supports(Class<?> clazz) {

        return Department.class.equals(clazz);
    }


    /**
     * Department name is mandantory Department name and description must have less than 200 characters.
     *
     * @param  target
     * @param  errors
     */
    @Override
    public void validate(Object target, Errors errors) {

        Department department = (Department) target;

        validateName(errors, department.getName());
        validateDescription(errors, department.getDescription());
    }


    private void validateName(Errors errors, String text) {

        boolean hasText = StringUtils.hasText(text);

        if (!hasText) {
            errors.rejectValue(FIELD_NAME, ERROR_REASON);
        }

        if (hasText && text.length() > MAX_CHARS_NAME) {
            errors.rejectValue(FIELD_NAME, ERROR_LENGTH);
        }
    }


    private void validateDescription(Errors errors, String description) {

        boolean hasText = StringUtils.hasText(description);

        if (hasText && description.length() > MAX_CHARS_DESC) {
            errors.rejectValue(FIELD_DESCRIPTION, ERROR_LENGTH);
        }
    }
}
