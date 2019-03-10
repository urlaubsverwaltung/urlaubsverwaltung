package org.synyx.urlaubsverwaltung.web.sicknote;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteComment;
import org.synyx.urlaubsverwaltung.core.workingtime.OverlapCase;
import org.synyx.urlaubsverwaltung.core.workingtime.OverlapService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link SickNoteValidator}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SickNoteValidatorTest {

    private SickNoteValidator validator;

    private OverlapService overlapService;
    private WorkingTimeService workingTimeService;

    private SickNote sickNote;
    private Errors errors;

    @Before
    public void setUp() throws Exception {

        overlapService = Mockito.mock(OverlapService.class);
        workingTimeService = Mockito.mock(WorkingTimeService.class);

        validator = new SickNoteValidator(overlapService, workingTimeService);
        errors = Mockito.mock(Errors.class);
        Mockito.reset(errors);

        sickNote = TestDataCreator.createSickNote(TestDataCreator.createPerson(),
                new DateMidnight(2013, DateTimeConstants.NOVEMBER, 19),
                new DateMidnight(2013, DateTimeConstants.NOVEMBER, 20), DayLength.FULL);

        when(overlapService.checkOverlap(Mockito.any(SickNote.class))).thenReturn(OverlapCase.NO_OVERLAPPING);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(Mockito.any(Person.class),
                    Mockito.any(DateMidnight.class)))
            .thenReturn(Optional.of(TestDataCreator.createWorkingTime()));
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
        verify(errors).rejectValue("dayLength", "error.entry.mandatory");
    }


    @Test
    public void ensureStartDateMayNotBeNull() {

        sickNote.setStartDate(null);
        validator.validate(sickNote, errors);
        verify(errors).rejectValue("startDate", "error.entry.mandatory");
    }


    @Test
    public void ensureEndDateMayNotBeNull() {

        sickNote.setEndDate(null);
        validator.validate(sickNote, errors);
        verify(errors).rejectValue("endDate", "error.entry.mandatory");
    }


    @Test
    public void ensureStartDateMustBeBeforeEndDateToHaveAValidPeriod() {

        sickNote.setStartDate(new DateMidnight(2013, DateTimeConstants.DECEMBER, 1));
        sickNote.setEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 19));
        validator.validate(sickNote, errors);
        verify(errors).rejectValue("endDate", "error.entry.invalidPeriod");
    }


    @Test
    public void ensureStartAndEndDateMustBeEqualsDatesForDayLengthNoon() {

        sickNote.setDayLength(DayLength.NOON);
        sickNote.setStartDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 19));
        sickNote.setEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 21));
        validator.validate(sickNote, errors);
        verify(errors).rejectValue("endDate", "sicknote.error.halfDayPeriod");
    }


    @Test
    public void ensureStartAndEndDateMustBeEqualsDatesForDayLengthMorning() {

        sickNote.setDayLength(DayLength.MORNING);
        sickNote.setStartDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 19));
        sickNote.setEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 21));
        validator.validate(sickNote, errors);
        verify(errors).rejectValue("endDate", "sicknote.error.halfDayPeriod");
    }


    @Test
    public void ensureStartDateMustBeBeforeEndDateToHaveAValidPeriodForDayLengthMorning() {

        sickNote.setDayLength(DayLength.MORNING);
        sickNote.setStartDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 21));
        sickNote.setEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 19));
        validator.validate(sickNote, errors);
        verify(errors).rejectValue("endDate", "error.entry.invalidPeriod");
    }


    @Test
    public void ensureStartDateMustBeBeforeEndDateToHaveAValidPeriodForDayLengthNoon() {

        sickNote.setDayLength(DayLength.NOON);
        sickNote.setStartDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 21));
        sickNote.setEndDate(new DateMidnight(2013, DateTimeConstants.NOVEMBER, 19));
        validator.validate(sickNote, errors);
        verify(errors).rejectValue("endDate", "error.entry.invalidPeriod");
    }


    @Test
    public void ensureCommentMayNotBeNull() {

        validator.validateComment(new SickNoteComment(), errors);
        validator.validate(sickNote, errors);
        verify(errors).rejectValue("text", "error.entry.mandatory");
    }


    @Test
    public void ensureTooLongCommentIsNotValid() {

        SickNoteComment comment = new SickNoteComment();

        comment.setText("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, "
            + "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, "
            + "sed diam voluptua. At vero eos et accusam et justo duo dolores bla bla");
        validator.validateComment(comment, errors);
        validator.validate(sickNote, errors);
        verify(errors).rejectValue("text", "error.entry.tooManyChars");
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
        verify(errors).rejectValue("aubEndDate", "error.entry.invalidPeriod");
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
        verify(errors).rejectValue("aubStartDate", "sicknote.error.aubInvalidPeriod");
        verify(errors).rejectValue("aubEndDate", "sicknote.error.aubInvalidPeriod");
    }


    @Test
    public void ensureSickNoteMustNotHaveAnyOverlapping() {

        sickNote.setStartDate(new DateMidnight(2015, DateTimeConstants.MARCH, 1));
        sickNote.setEndDate(new DateMidnight(2015, DateTimeConstants.MARCH, 10));

        when(overlapService.checkOverlap(Mockito.any(SickNote.class)))
            .thenReturn(OverlapCase.FULLY_OVERLAPPING);

        validator.validate(sickNote, errors);

        verify(errors).reject("application.error.overlap");
    }


    @Test
    public void ensureWorkingTimeConfigurationMustExistForPeriodOfSickNote() {

        DateMidnight startDate = new DateMidnight(2015, DateTimeConstants.MARCH, 1);
        DateMidnight endDate = new DateMidnight(2015, DateTimeConstants.MARCH, 10);

        sickNote.setStartDate(startDate);
        sickNote.setEndDate(endDate);

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(Mockito.any(Person.class),
                    Mockito.any(DateMidnight.class)))
            .thenReturn(Optional.empty());

        validator.validate(sickNote, errors);

        verify(workingTimeService).getByPersonAndValidityDateEqualsOrMinorDate(sickNote.getPerson(), startDate);
        verify(errors).reject("sicknote.error.noValidWorkingTime");
    }


    @Test
    public void ensureInvalidPeriodWithValidAUBPeriodIsNotValid() {

        DateMidnight startDate = new DateMidnight(2016, DateTimeConstants.MARCH, 16);
        DateMidnight endDate = new DateMidnight(2016, DateTimeConstants.MARCH, 14);

        DateMidnight aubStartDate = new DateMidnight(2016, DateTimeConstants.MARCH, 14);
        DateMidnight aubEndDate = new DateMidnight(2016, DateTimeConstants.MARCH, 16);

        sickNote.setStartDate(startDate);
        sickNote.setEndDate(endDate);

        sickNote.setAubStartDate(aubStartDate);
        sickNote.setAubEndDate(aubEndDate);

        validator.validate(sickNote, errors);

        verify(errors).rejectValue("endDate", "error.entry.invalidPeriod");
    }


    @Test
    public void ensureInvalidAUBPeriodWithValidPeriodIsNotValid() {

        DateMidnight startDate = new DateMidnight(2016, DateTimeConstants.MARCH, 14);
        DateMidnight endDate = new DateMidnight(2016, DateTimeConstants.MARCH, 16);

        DateMidnight aubStartDate = new DateMidnight(2016, DateTimeConstants.MARCH, 16);
        DateMidnight aubEndDate = new DateMidnight(2016, DateTimeConstants.MARCH, 14);

        sickNote.setStartDate(startDate);
        sickNote.setEndDate(endDate);

        sickNote.setAubStartDate(aubStartDate);
        sickNote.setAubEndDate(aubEndDate);

        validator.validate(sickNote, errors);

        verify(errors).rejectValue("aubEndDate", "error.entry.invalidPeriod");
    }
}
