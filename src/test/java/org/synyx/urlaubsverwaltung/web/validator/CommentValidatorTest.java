package org.synyx.urlaubsverwaltung.web.validator;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.validation.Errors;

import org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteComment;
import org.synyx.urlaubsverwaltung.web.application.CommentForm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.web.validator.CommentValidator}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class CommentValidatorTest {

    private CommentValidator validator;
    private Errors errors;

    @Before
    public void setUp() {

        validator = new CommentValidator();
        errors = Mockito.mock(Errors.class);
    }


    @Test
    public void ensureSupportsCommentClass() {

        assertTrue(validator.supports(CommentForm.class));
    }


    @Test
    public void ensureDoesNotSupportNull() {

        assertFalse(validator.supports(null));
    }


    @Test
    public void ensureDoesNotSupportOtherClass() {

        assertFalse(validator.supports(SickNoteComment.class));
    }


    @Test
    public void ensureReasonCanBeNullIfNotMandatory() {

        CommentForm comment = new CommentForm();
        comment.setMandatory(false);
        comment.setText(null);

        validator.validate(comment, errors);

        Mockito.verifyZeroInteractions(errors);
    }


    @Test
    public void ensureReasonCanBeEmptyIfNotMandatory() {

        CommentForm comment = new CommentForm();
        comment.setMandatory(false);
        comment.setText("");

        validator.validate(comment, errors);

        Mockito.verifyZeroInteractions(errors);
    }


    @Test
    public void ensureReasonCanNotBeNullIfMandatory() {

        CommentForm comment = new CommentForm();
        comment.setMandatory(true);
        comment.setText(null);

        validator.validate(comment, errors);

        Mockito.verify(errors).rejectValue("reason", "error.reason");
    }


    @Test
    public void ensureReasonCanNotBeEmptyIfMandatory() {

        CommentForm comment = new CommentForm();
        comment.setMandatory(true);
        comment.setText("");

        validator.validate(comment, errors);

        Mockito.verify(errors).rejectValue("reason", "error.reason");
    }


    @Test
    public void ensureThereIsAMaximumCharLengthForReason() {

        CommentForm comment = new CommentForm();

        comment.setText(
            "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt"
            + " ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud "
            + "exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. ");

        validator.validate(comment, errors);

        Mockito.verify(errors).rejectValue("reason", "error.length");
    }
}
