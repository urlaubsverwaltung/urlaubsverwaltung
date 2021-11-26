package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.Errors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit test for {@link SickNoteConvertFormValidator}.
 */
class SickNoteConvertFormValidatorTest {

    private SickNoteConvertFormValidator validator;

    private Errors errors;

    @BeforeEach
    void setUp() {
        validator = new SickNoteConvertFormValidator();
        errors = mock(Errors.class);
    }

    @Test
    void ensureNullReasonIsNotValid() {

        final SickNoteConvertForm convertForm = new SickNoteConvertForm(new SickNote());
        convertForm.setReason(null);

        validator.validate(convertForm, errors);

        verify(errors).rejectValue("reason", "error.entry.mandatory");
    }

    @Test
    void ensureEmptyReasonIsNotValid() {

        final SickNoteConvertForm convertForm = new SickNoteConvertForm(new SickNote());
        convertForm.setReason("");

        validator.validate(convertForm, errors);

        verify(errors).rejectValue("reason", "error.entry.mandatory");
    }

    @Test
    void ensureThereIsAMaximumCharLengthForReason() {

        final SickNoteConvertForm convertForm = new SickNoteConvertForm(new SickNote());
        convertForm.setReason(
            "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt"
                + " ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud "
                + "exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. ");

        validator.validate(convertForm, errors);

        verify(errors).rejectValue("reason", "error.entry.tooManyChars");
    }
}
