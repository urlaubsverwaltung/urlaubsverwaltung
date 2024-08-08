package org.synyx.urlaubsverwaltung.sicknote.comment;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.springframework.util.StringUtils.hasText;

/**
 * Class for validating {@link SickNoteCommentFormDto} object.
 */
@Component
public class SickNoteCommentFormValidator implements Validator {

    private static final String ATTRIBUTE_TEXT = "text";
    private static final String ERROR_REASON = "sicknote.action.reason.error.mandatory";

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return SickNoteCommentFormDto.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        final SickNoteCommentFormDto comment = (SickNoteCommentFormDto) target;

        final String text = comment.getText();
        if (!hasText(text) && comment.isMandatory()) {
            errors.rejectValue(ATTRIBUTE_TEXT, ERROR_REASON);
        }
    }
}
