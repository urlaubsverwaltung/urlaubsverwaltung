package org.synyx.urlaubsverwaltung.application.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteComment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;


/**
 * Unit test for {@link ApplicationCommentValidator}.
 */
class ApplicationCommentValidatorTest {

    private ApplicationCommentValidator validator;
    private Errors errors;

    @BeforeEach
    void setUp() {

        validator = new ApplicationCommentValidator();
        errors = mock(Errors.class);
    }

    @Test
    void ensureSupportsCommentClass() {
        assertThat(validator.supports(ApplicationCommentForm.class)).isTrue();
    }

    @Test
    void ensureDoesNotSupportNull() {
        assertThat(validator.supports(null)).isFalse();
    }

    @Test
    void ensureDoesNotSupportOtherClass() {
        assertThat(validator.supports(SickNoteComment.class)).isFalse();
    }

    @Test
    void ensureReasonCanBeNullIfNotMandatory() {

        final ApplicationCommentForm comment = new ApplicationCommentForm();
        comment.setMandatory(false);
        comment.setText(null);

        validator.validate(comment, errors);

        verifyNoInteractions(errors);
    }

    @Test
    void ensureReasonCanBeEmptyIfNotMandatory() {

        final ApplicationCommentForm comment = new ApplicationCommentForm();
        comment.setMandatory(false);
        comment.setText("");

        validator.validate(comment, errors);

        verifyNoInteractions(errors);
    }

    @Test
    void ensureReasonCanNotBeNullIfMandatory() {

        final ApplicationCommentForm comment = new ApplicationCommentForm();
        comment.setMandatory(true);
        comment.setText(null);

        validator.validate(comment, errors);

        verify(errors).rejectValue("text", "error.entry.mandatory");
    }

    @Test
    void ensureReasonCanNotBeEmptyIfMandatory() {

        final ApplicationCommentForm comment = new ApplicationCommentForm();
        comment.setMandatory(true);
        comment.setText("");

        validator.validate(comment, errors);

        verify(errors).rejectValue("text", "error.entry.mandatory");
    }

    @Test
    void ensureThereIsAMaximumCharLengthForReason() {

        final ApplicationCommentForm comment = new ApplicationCommentForm();

        comment.setText(
            "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt"
                + " ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud "
                + "exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. ");

        validator.validate(comment, errors);

        verify(errors).rejectValue("text", "error.entry.tooManyChars");
    }
}
