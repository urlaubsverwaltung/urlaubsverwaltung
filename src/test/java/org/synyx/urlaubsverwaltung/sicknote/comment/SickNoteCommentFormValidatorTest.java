package org.synyx.urlaubsverwaltung.sicknote.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SickNoteCommentFormValidatorTest {

    private SickNoteCommentFormValidator sut;

    @BeforeEach
    void setUp() {
        sut = new SickNoteCommentFormValidator();
    }

    @Test
    void ensureCommentMayNotBeNull() {
        final SickNoteCommentForm sickNoteCommentForm = new SickNoteCommentForm();

        final Errors errors = new BeanPropertyBindingResult(sickNoteCommentForm, "sickNote");
        sut.validate(sickNoteCommentForm, errors);
        assertThat(errors.getFieldErrors("text").get(0).getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    void ensureTooLongCommentIsNotValid() {

        final SickNoteCommentForm sickNoteCommentForm = new SickNoteCommentForm();
        sickNoteCommentForm.setText("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, "
            + "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, "
            + "sed diam voluptua. At vero eos et accusam et justo duo dolores bla bla");

        final Errors errors = new BeanPropertyBindingResult(sickNoteCommentForm, "sickNote");
        sut.validate(sickNoteCommentForm, errors);
        assertThat(errors.getFieldErrors("text").get(0).getCode()).isEqualTo("error.entry.tooManyChars");
    }

    @Test
    void ensureValidCommentHasNoErrors() {
        final SickNoteCommentForm sickNoteCommentForm = new SickNoteCommentForm();
        sickNoteCommentForm.setText("I am a fluffy little comment");

        final Errors errors = new BeanPropertyBindingResult(sickNoteCommentForm, "sickNote");
        sut.validate(sickNoteCommentForm, errors);
        assertThat(errors.getErrorCount()).isZero();
    }
}
