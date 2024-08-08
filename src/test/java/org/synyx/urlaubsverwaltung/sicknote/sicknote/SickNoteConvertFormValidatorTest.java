package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit test for {@link SickNoteConvertFormValidator}.
 */
@ExtendWith(MockitoExtension.class)
class SickNoteConvertFormValidatorTest {

    private SickNoteConvertFormValidator validator;

    @Mock
    private Errors errors;

    @BeforeEach
    void setUp() {
        validator = new SickNoteConvertFormValidator();
    }

    @Test
    void ensureIfReasonIsGivenNoError() {

        final SickNoteConvertForm convertForm = new SickNoteConvertForm(SickNote.builder().build());
        convertForm.setReason("reason");

        validator.validate(convertForm, errors);

        verifyNoInteractions(errors);
    }

    @Test
    void ensureNullReasonIsNotValid() {

        final SickNoteConvertForm convertForm = new SickNoteConvertForm(SickNote.builder().build());
        convertForm.setReason(null);

        validator.validate(convertForm, errors);

        verify(errors).rejectValue("reason", "error.entry.mandatory");
    }

    @Test
    void ensureEmptyReasonIsNotValid() {

        final SickNoteConvertForm convertForm = new SickNoteConvertForm(SickNote.builder().build());
        convertForm.setReason("");

        validator.validate(convertForm, errors);

        verify(errors).rejectValue("reason", "error.entry.mandatory");
    }
}
