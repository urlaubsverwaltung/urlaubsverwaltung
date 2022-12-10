package org.synyx.urlaubsverwaltung.sicknote.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SickNoteCommentFormDtoValidatorTest {

    private SickNoteCommentFormValidator sut;

    @BeforeEach
    void setUp() {
        sut = new SickNoteCommentFormValidator();
    }

    @Test
    void ensureCommentMayNotBeNull() {
        final SickNoteCommentFormDto sickNoteCommentFormDto = new SickNoteCommentFormDto();

        final Errors errors = new BeanPropertyBindingResult(sickNoteCommentFormDto, "sickNote");
        sut.validate(sickNoteCommentFormDto, errors);
        assertThat(errors.getFieldErrors("text").get(0).getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    void ensureTooLongCommentIsNotValid() {

        final SickNoteCommentFormDto sickNoteCommentFormDto = new SickNoteCommentFormDto();
        sickNoteCommentFormDto.setText("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, "
            + "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, "
            + "sed diam voluptua. At vero eos et accusam et justo duo dolores bla bla");

        final Errors errors = new BeanPropertyBindingResult(sickNoteCommentFormDto, "sickNote");
        sut.validate(sickNoteCommentFormDto, errors);
        assertThat(errors.getFieldErrors("text").get(0).getCode()).isEqualTo("error.entry.tooManyChars");
    }

    @Test
    void ensureValidCommentHasNoErrors() {
        final SickNoteCommentFormDto sickNoteCommentFormDto = new SickNoteCommentFormDto();
        sickNoteCommentFormDto.setText("I am a fluffy little comment");

        final Errors errors = new BeanPropertyBindingResult(sickNoteCommentFormDto, "sickNote");
        sut.validate(sickNoteCommentFormDto, errors);
        assertThat(errors.getErrorCount()).isZero();
    }
}
