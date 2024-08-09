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
    void ensureSupportsCommentClass() {
        assertThat(sut.supports(SickNoteCommentFormDto.class)).isTrue();
    }

    @Test
    void ensureDoesNotSupportNull() {
        assertThat(sut.supports(null)).isFalse();
    }

    @Test
    void ensureDoesNotSupportOtherClass() {
        assertThat(sut.supports(Object.class)).isFalse();
    }

    @Test
    void ensureCommentCanBeNullIfNotMandatory() {

        final SickNoteCommentFormDto sickNoteCommentFormDto = new SickNoteCommentFormDto();
        sickNoteCommentFormDto.setMandatory(false);
        sickNoteCommentFormDto.setText(null);

        final Errors errors = new BeanPropertyBindingResult(sickNoteCommentFormDto, "sickNote");
        sut.validate(sickNoteCommentFormDto, errors);
        assertThat(errors.getFieldErrors("text")).isEmpty();
    }

    @Test
    void ensureCommentCanBeEmptyIfNotMandatory() {

        final SickNoteCommentFormDto sickNoteCommentFormDto = new SickNoteCommentFormDto();
        sickNoteCommentFormDto.setMandatory(false);
        sickNoteCommentFormDto.setText("");

        final Errors errors = new BeanPropertyBindingResult(sickNoteCommentFormDto, "sickNote");
        sut.validate(sickNoteCommentFormDto, errors);
        assertThat(errors.getFieldErrors("text")).isEmpty();
    }

    @Test
    void ensureCommentCanNotBeNullIfMandatory() {

        final SickNoteCommentFormDto sickNoteCommentFormDto = new SickNoteCommentFormDto();
        sickNoteCommentFormDto.setMandatory(true);
        sickNoteCommentFormDto.setText(null);

        final Errors errors = new BeanPropertyBindingResult(sickNoteCommentFormDto, "sickNote");
        sut.validate(sickNoteCommentFormDto, errors);
        assertThat(errors.getFieldErrors("text").get(0).getCode()).isEqualTo("sicknote.action.reason.error.mandatory");
    }

    @Test
    void ensureCommentCanNotBeEmptyIfMandatory() {

        final SickNoteCommentFormDto comment = new SickNoteCommentFormDto();
        comment.setMandatory(true);
        comment.setText("");

        final Errors errors = new BeanPropertyBindingResult(comment, "sickNote");
        sut.validate(comment, errors);
        assertThat(errors.getFieldErrors("text").get(0).getCode()).isEqualTo("sicknote.action.reason.error.mandatory");
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
