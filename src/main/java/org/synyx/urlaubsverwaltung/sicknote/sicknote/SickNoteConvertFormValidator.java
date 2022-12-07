package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Class for validating {@link SickNoteConvertForm} object.
 */
@Component
public class SickNoteConvertFormValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_LENGTH = "error.entry.tooManyChars";

    private static final int MAX_CHARS = 200;

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return SickNoteConvertForm.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {

        final SickNoteConvertForm convertForm = (SickNoteConvertForm) target;
        final String reason = convertForm.getReason();

        if (!StringUtils.hasText(reason)) {
            errors.rejectValue("reason", ERROR_MANDATORY_FIELD);
        } else {
            if (reason.length() > MAX_CHARS) {
                errors.rejectValue("reason", ERROR_LENGTH);
            }
        }
    }
}
