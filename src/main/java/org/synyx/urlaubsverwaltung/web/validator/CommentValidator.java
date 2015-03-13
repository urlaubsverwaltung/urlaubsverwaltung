package org.synyx.urlaubsverwaltung.web.validator;

import org.springframework.stereotype.Component;

import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.web.application.CommentForm;


/**
 * Validates the content of {@link org.synyx.urlaubsverwaltung.web.application.CommentForm}s.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Component
public class CommentValidator implements Validator {

    private static final int MAX_CHARS = 200;

    private static final String FIELD_REASON = "reason";

    private static final String ERROR_REASON = "error.reason";
    private static final String ERROR_LENGTH = "error.length";

    @Override
    public boolean supports(Class<?> clazz) {

        return CommentForm.class.equals(clazz);
    }


    @Override
    public void validate(Object target, Errors errors) {

        CommentForm comment = (CommentForm) target;

        String text = comment.getText();
        boolean hasText = StringUtils.hasText(text);

        if (!hasText && comment.isMandatory()) {
            errors.rejectValue(FIELD_REASON, ERROR_REASON);
        }

        if (hasText && text.length() > MAX_CHARS) {
            errors.rejectValue(FIELD_REASON, ERROR_LENGTH);
        }
    }
}
