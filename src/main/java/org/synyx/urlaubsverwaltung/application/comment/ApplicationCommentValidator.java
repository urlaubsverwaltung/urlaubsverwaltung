package org.synyx.urlaubsverwaltung.application.comment;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.springframework.util.StringUtils.hasText;

/**
 * Validates the content of {@link ApplicationCommentForm}s.
 */
@Component
public class ApplicationCommentValidator implements Validator {

    private static final String ATTRIBUTE_TEXT = "text";
    private static final String ERROR_REASON = "error.entry.mandatory";

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return ApplicationCommentForm.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        final ApplicationCommentForm comment = (ApplicationCommentForm) target;

        final String text = comment.getText();
        if (!hasText(text) && comment.isMandatory()) {
            errors.rejectValue(ATTRIBUTE_TEXT, ERROR_REASON);
        }
    }
}
