package org.synyx.urlaubsverwaltung.web.sicknote;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.validation.Errors;

import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.calendar.OverlapCase;
import org.synyx.urlaubsverwaltung.core.calendar.OverlapService;
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

    private OverlapService overlapService;

    private SickNote sickNote;
    private Errors errors;

    @Before
    public void setUp() throws Exception {

        overlapService = Mockito.mock(OverlapService.class);

        validator = new SickNoteValidator(overlapService);
        sickNote = new SickNote();
        errors = Mockito.mock(Errors.class);
        Mockito.reset(errors);

        sickNote.setPerson(new Person());
        sickNote.setDayLength(DayLength.FULL);
        sickNote.setStartDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 19));
        sickNote.setEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 20));
    }


    @Test
    public void ensureValidDatesHaveNoErrors() {

        validator.validate(sickNote, errors);
        Mockito.verifyZeroInteractions(errors);
    }


    @Test
    public void ensureDayLengthMayNotBeNull() {

        sickNote.setDayLength(null);
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("dayLength", "error.entry.mandatory");
    }


    @Test
    public void ensureStartDateMayNotBeNull() {

        sickNote.setStartDate(null);
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("startDate", "error.entry.mandatory");
    }


    @Test
    public void ensureEndDateMayNotBeNull() {

        sickNote.setEndDate(null);
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("endDate", "error.entry.mandatory");
    }


    @Test
    public void ensureStartDateMustBeBeforeEndDateToHaveAValidPeriod() {

        sickNote.setStartDate(new DateMidnight(2013, DateTimeConstants.DECEMBER, 1));
        sickNote.setEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 19));
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("endDate", "error.entry.invalidPeriod");
    }


    @Test
    public void ensureStartAndEndDateMustBeEqualsDatesForDayLengthNoon() {

        sickNote.setDayLength(DayLength.NOON);
        sickNote.setStartDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 19));
        sickNote.setEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 21));
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("endDate", "sicknote.error.halfDayPeriod");
    }


    @Test
    public void ensureStartAndEndDateMustBeEqualsDatesForDayLengthMorning() {

        sickNote.setDayLength(DayLength.MORNING);
        sickNote.setStartDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 19));
        sickNote.setEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 21));
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("endDate", "sicknote.error.halfDayPeriod");
    }


    @Test
    public void ensureStartDateMustBeBeforeEndDateToHaveAValidPeriodForDayLengthMorning() {

        sickNote.setDayLength(DayLength.MORNING);
        sickNote.setStartDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 21));
        sickNote.setEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 19));
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("endDate", "error.entry.invalidPeriod");
    }


    @Test
    public void ensureStartDateMustBeBeforeEndDateToHaveAValidPeriodForDayLengthNoon() {

        sickNote.setDayLength(DayLength.NOON);
        sickNote.setStartDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 21));
        sickNote.setEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 19));
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("endDate", "error.entry.invalidPeriod");
    }


    @Test
    public void ensureCommentMayNotBeNull() {

        validator.validateComment(new SickNoteComment(), errors);
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("text", "error.entry.mandatory");
    }


    @Test
    public void ensureTooLongCommentIsNotValid() {

        SickNoteComment comment = new SickNoteComment();

        comment.setText("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, "
            + "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, "
            + "sed diam voluptua. At vero eos et accusam et justo duo dolores bla bla");
        validator.validateComment(comment, errors);
        validator.validate(sickNote, errors);
        Mockito.verify(errors).rejectValue("text", "error.entry.tooManyChars");
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
        Mockito.verify(errors).rejectValue("aubEndDate", "error.entry.invalidPeriod");
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
        Mockito.verify(errors).rejectValue("aubStartDate", "sicknote.error.aubInvalidPeriod");
        Mockito.verify(errors).rejectValue("aubEndDate", "sicknote.error.aubInvalidPeriod");
    }


    @Test
    public void ensureSickNoteMustNotHaveAnyOverlapping() {

        sickNote.setStartDate(new DateMidnight(2015, DateTimeConstants.MARCH, 1));
        sickNote.setEndDate(new DateMidnight(2015, DateTimeConstants.MARCH, 10));

        Mockito.when(overlapService.checkOverlap(Mockito.any(SickNote.class)))
            .thenReturn(OverlapCase.FULLY_OVERLAPPING);

        validator.validate(sickNote, errors);

        Mockito.verify(errors).reject("application.error.overlap");
    }
}
