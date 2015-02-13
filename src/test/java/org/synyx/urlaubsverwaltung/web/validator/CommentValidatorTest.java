package org.synyx.urlaubsverwaltung.web.validator;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.validation.Errors;

import org.synyx.urlaubsverwaltung.core.application.domain.Comment;
import org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteComment;

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

        assertTrue(validator.supports(Comment.class));
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

        Comment comment = new Comment();
        comment.setMandatory(false);
        comment.setReason(null);

        validator.validate(comment, errors);

        Mockito.verifyZeroInteractions(errors);
    }


    @Test
    public void ensureReasonCanBeEmptyIfNotMandatory() {

        Comment comment = new Comment();
        comment.setMandatory(false);
        comment.setReason("");

        validator.validate(comment, errors);

        Mockito.verifyZeroInteractions(errors);
    }


    @Test
    public void ensureReasonCanNotBeNullIfMandatory() {

        Comment comment = new Comment();
        comment.setMandatory(true);
        comment.setReason(null);

        validator.validate(comment, errors);

        Mockito.verify(errors).rejectValue("reason", "error.reason");
    }


    @Test
    public void ensureReasonCanNotBeEmptyIfMandatory() {

        Comment comment = new Comment();
        comment.setMandatory(true);
        comment.setReason("");

        validator.validate(comment, errors);

        Mockito.verify(errors).rejectValue("reason", "error.reason");
    }


    @Test
    public void ensureThereIsAMaximumCharLengthForReason() {

        Comment comment = new Comment();

        comment.setReason(
            "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt"
            + " ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud "
            + "exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. ");

        validator.validate(comment, errors);

        Mockito.verify(errors).rejectValue("reason", "error.length");
    }
}
