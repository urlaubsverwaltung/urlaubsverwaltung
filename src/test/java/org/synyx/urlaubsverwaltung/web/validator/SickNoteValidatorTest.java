package org.synyx.urlaubsverwaltung.web.validator;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.validation.Errors;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteComment;


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
    }


    @Test
    public void ensureValidDatesHaveNoErrors() {

        validator.validate(sickNote, errors);
        Mockito.verifyZeroInteractions(errors);
    }


    @Test
    public void ensureStartDateMayNotBeNull() {

        sickNote.setStartDate(null);
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("startDate", "error.mandatory.field");
        Mockito.reset(errors);
    }


    @Test
    public void ensureEndDateMayNotBeNull() {

        sickNote.setEndDate(null);
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("endDate", "error.mandatory.field");
        Mockito.reset(errors);
    }


    @Test
    public void ensureStartDateMustBeBeforeEndDateToHaveAValidPeriod() {

        sickNote.setStartDate(new DateMidnight(2013, DateTimeConstants.DECEMBER, 1));
        sickNote.setEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 19));
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("endDate", "error.period");
        Mockito.reset(errors);
    }


    @Test
    public void ensureCommentMayNotBeNull() {

        validator.validateComment(new SickNoteComment(), errors);
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("text", "error.mandatory.field");
        Mockito.reset(errors);
    }


    @Test
    public void ensureTooLongCommentIsNotValid() {

        SickNoteComment comment = new SickNoteComment();

        comment.setText("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, "
            + "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, "
            + "sed diam voluptua. At vero eos et accusam et justo duo dolores bla bla");
        validator.validateComment(comment, errors);
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("text", "error.length");
        Mockito.reset(errors);
    }


    @Test
    public void ensureValidCommentHasNoErrors() {

        SickNoteComment comment = new SickNoteComment();

        comment.setText("I am a fluffy little comment");
        validator.validateComment(comment, errors);
        validator.validate(sickNote, errors);
        Mockito.verifyZeroInteractions(errors);
    }


    @Test
    public void ensureAUStartDateMustBeBeforeAUEndDateToHaveAValidPeriod() {

        sickNote.setAubStartDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 20));
        sickNote.setAubEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 19));
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("aubEndDate", "error.period");
        Mockito.reset(errors);
    }


    @Test
    public void ensureValidAUPeriodHasNoErrors() {

        sickNote.setAubStartDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 19));
        sickNote.setAubEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 20));
        validator.validate(sickNote, errors);
        Mockito.verifyZeroInteractions(errors);
    }


    @Test
    public void ensureAUPeriodMustBeWithinSickNotePeriod() {

        sickNote.setAubStartDate(new DateMidnight(2013, DateTimeConstants.DECEMBER, 19));
        sickNote.setAubEndDate(new DateMidnight(2013, DateTimeConstants.DECEMBER, 20));
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("aubStartDate", "error.period.sicknote");
        Mockito.verify(errors).rejectValue("aubEndDate", "error.period.sicknote");
    }
}
