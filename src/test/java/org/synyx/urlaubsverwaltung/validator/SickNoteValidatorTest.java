package org.synyx.urlaubsverwaltung.validator;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.validation.Errors;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteComment;


/**
 * Unit test for {@link SickNoteValidator}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SickNoteValidatorTest {

    private SickNoteValidator validator;
    private SickNote sickNote;
    private Errors errors;

    @Before
    public void setUp() throws Exception {

        validator = new SickNoteValidator();
        sickNote = new SickNote();
        errors = Mockito.mock(Errors.class);
        Mockito.reset(errors);

        sickNote.setPerson(new Person());
        sickNote.setStartDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 19));
        sickNote.setEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 20));
        sickNote.setAubPresent(true);
    }


    @Test
    public void testValidateStartDate() {

        validator.validate(sickNote, errors);
        Mockito.verifyZeroInteractions(errors);

        sickNote.setStartDate(null);
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("startDate", "error.mandatory.field");
        Mockito.reset(errors);
    }


    @Test
    public void testValidateEndDate() {

        validator.validate(sickNote, errors);
        Mockito.verifyZeroInteractions(errors);

        sickNote.setEndDate(null);
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("endDate", "error.mandatory.field");
        Mockito.reset(errors);
    }


    @Test
    public void testValidatePeriod() {

        validator.validate(sickNote, errors);
        Mockito.verifyZeroInteractions(errors);

        sickNote.setStartDate(new DateMidnight(2013, DateTimeConstants.DECEMBER, 1));
        sickNote.setEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 19));
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("endDate", "error.period");
        Mockito.reset(errors);
    }


    @Test
    public void testValidateComment() {

        SickNoteComment comment = new SickNoteComment();

        validator.validateComment(comment, errors);
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("text", "error.mandatory.field");
        Mockito.reset(errors);

        comment.setText(
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores bla bla");
        validator.validateComment(comment, errors);
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("text", "error.length");
        Mockito.reset(errors);

        comment.setText("I am a fluffy little comment");
        validator.validateComment(comment, errors);
        validator.validate(sickNote, errors);
        Mockito.verifyZeroInteractions(errors);
    }
}
