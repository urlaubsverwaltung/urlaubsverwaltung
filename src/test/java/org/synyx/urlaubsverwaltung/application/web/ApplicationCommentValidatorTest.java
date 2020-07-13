package org.synyx.urlaubsverwaltung.application.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteComment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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

        assertTrue(validator.supports(ApplicationCommentForm.class));
    }


    @Test
    void ensureDoesNotSupportNull() {

        assertFalse(validator.supports(null));
    }


    @Test
    void ensureDoesNotSupportOtherClass() {

        assertFalse(validator.supports(SickNoteComment.class));
    }


    @Test
    void ensureReasonCanBeNullIfNotMandatory() {

        ApplicationCommentForm comment = new ApplicationCommentForm();
        comment.setMandatory(false);
        comment.setText(null);

        validator.validate(comment, errors);

        verifyNoInteractions(errors);
    }


    @Test
    void ensureReasonCanBeEmptyIfNotMandatory() {

        ApplicationCommentForm comment = new ApplicationCommentForm();
        comment.setMandatory(false);
        comment.setText("");

        validator.validate(comment, errors);

        verifyNoInteractions(errors);
    }


    @Test
    void ensureReasonCanNotBeNullIfMandatory() {

        ApplicationCommentForm comment = new ApplicationCommentForm();
        comment.setMandatory(true);
        comment.setText(null);

        validator.validate(comment, errors);

        verify(errors).rejectValue("text", "error.entry.mandatory");
    }


    @Test
    void ensureReasonCanNotBeEmptyIfMandatory() {

        ApplicationCommentForm comment = new ApplicationCommentForm();
        comment.setMandatory(true);
        comment.setText("");

        validator.validate(comment, errors);

        verify(errors).rejectValue("text", "error.entry.mandatory");
    }


    @Test
    void ensureThereIsAMaximumCharLengthForReason() {

        ApplicationCommentForm comment = new ApplicationCommentForm();

        comment.setText(
            "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt"
                + " ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud "
                + "exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. ");

        validator.validate(comment, errors);

        verify(errors).rejectValue("text", "error.entry.tooManyChars");
    }
}
