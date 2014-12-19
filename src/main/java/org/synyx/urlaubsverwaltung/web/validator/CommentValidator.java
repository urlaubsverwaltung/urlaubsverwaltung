package org.synyx.urlaubsverwaltung.web.validator;

import org.springframework.stereotype.Component;

import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.application.domain.Comment;


/**
 * Validates the content of {@link org.synyx.urlaubsverwaltung.core.application.domain.Comment}s.
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

        return Comment.class.equals(clazz);
    }


    @Override
    public void validate(Object target, Errors errors) {

        Comment comment = (Comment) target;

        boolean reasonIsGiven = StringUtils.hasText(comment.getReason());

        if (!reasonIsGiven && comment.isMandatory()) {
            errors.rejectValue(FIELD_REASON, ERROR_REASON);
        }

        if (reasonIsGiven && comment.getReason().length() > MAX_CHARS) {
            errors.rejectValue(FIELD_REASON, ERROR_LENGTH);
        }
    }
}
