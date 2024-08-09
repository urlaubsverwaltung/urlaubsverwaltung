package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.springframework.util.StringUtils.hasText;

/**
 * Class for validating {@link SickNoteConvertForm} object.
 */
@Component
public class SickNoteConvertFormValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return SickNoteConvertForm.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {

        final SickNoteConvertForm convertForm = (SickNoteConvertForm) target;
        final String reason = convertForm.getReason();

        if (!hasText(reason)) {
            errors.rejectValue("reason", ERROR_MANDATORY_FIELD);
        }
    }
}
