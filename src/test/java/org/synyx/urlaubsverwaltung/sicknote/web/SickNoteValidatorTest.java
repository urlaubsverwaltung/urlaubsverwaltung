package org.synyx.urlaubsverwaltung.sicknote.web;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteComment;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;
import org.synyx.urlaubsverwaltung.workingtime.OverlapCase;
import org.synyx.urlaubsverwaltung.workingtime.OverlapService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.LocalDate;
import java.util.Optional;

import static java.time.Month.DECEMBER;
import static java.time.Month.MARCH;
import static java.time.Month.NOVEMBER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link SickNoteValidator}.
 */
public class SickNoteValidatorTest {

    private SickNoteValidator validator;

    private OverlapService overlapService;
    private WorkingTimeService workingTimeService;

    private SickNote sickNote;
    private Errors errors;

    @Before
    public void setUp() {

        overlapService = mock(OverlapService.class);
        workingTimeService = mock(WorkingTimeService.class);

        validator = new SickNoteValidator(overlapService, workingTimeService);
        errors = mock(Errors.class);
        Mockito.reset(errors);

        sickNote = TestDataCreator.createSickNote(TestDataCreator.createPerson(),
            LocalDate.of(2013, NOVEMBER, 19),
            LocalDate.of(2013, NOVEMBER, 20),
            DayLength.FULL);

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(OverlapCase.NO_OVERLAPPING);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class),
            any(LocalDate.class)))
            .thenReturn(Optional.of(TestDataCreator.createWorkingTime()));
    }


    @Test
    public void ensureValidDatesHaveNoErrors() {

        validator.validate(sickNote, errors);
        verifyZeroInteractions(errors);
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

        sickNote.setStartDate(LocalDate.of(2013, DECEMBER, 1));
        sickNote.setEndDate(LocalDate.of(2013, NOVEMBER, 19));
        validator.validate(sickNote, errors);
        verify(errors).rejectValue("endDate", "error.entry.invalidPeriod");
    }


    @Test
    public void ensureStartAndEndDateMustBeEqualsDatesForDayLengthNoon() {

        sickNote.setDayLength(DayLength.NOON);
        sickNote.setStartDate(LocalDate.of(2013, NOVEMBER, 19));
        sickNote.setEndDate(LocalDate.of(2013, NOVEMBER, 21));
        validator.validate(sickNote, errors);
        verify(errors).rejectValue("endDate", "sicknote.error.halfDayPeriod");
    }


    @Test
    public void ensureStartAndEndDateMustBeEqualsDatesForDayLengthMorning() {

        sickNote.setDayLength(DayLength.MORNING);
        sickNote.setStartDate(LocalDate.of(2013, NOVEMBER, 19));
        sickNote.setEndDate(LocalDate.of(2013, NOVEMBER, 21));
        validator.validate(sickNote, errors);
        verify(errors).rejectValue("endDate", "sicknote.error.halfDayPeriod");
    }


    @Test
    public void ensureStartDateMustBeBeforeEndDateToHaveAValidPeriodForDayLengthMorning() {

        sickNote.setDayLength(DayLength.MORNING);
        sickNote.setStartDate(LocalDate.of(2013, NOVEMBER, 21));
        sickNote.setEndDate(LocalDate.of(2013, NOVEMBER, 19));
        validator.validate(sickNote, errors);
        verify(errors).rejectValue("endDate", "error.entry.invalidPeriod");
    }


    @Test
    public void ensureStartDateMustBeBeforeEndDateToHaveAValidPeriodForDayLengthNoon() {

        sickNote.setDayLength(DayLength.NOON);
        sickNote.setStartDate(LocalDate.of(2013, NOVEMBER, 21));
        sickNote.setEndDate(LocalDate.of(2013, NOVEMBER, 19));
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
        verifyZeroInteractions(errors);
    }


    @Test
    public void ensureAUStartDateMustBeBeforeAUEndDateToHaveAValidPeriod() {

        sickNote.setAubStartDate(LocalDate.of(2013, NOVEMBER, 20));
        sickNote.setAubEndDate(LocalDate.of(2013, NOVEMBER, 19));
        validator.validate(sickNote, errors);
        verify(errors).rejectValue("aubEndDate", "error.entry.invalidPeriod");
    }


    @Test
    public void ensureValidAUPeriodHasNoErrors() {

        sickNote.setAubStartDate(LocalDate.of(2013, NOVEMBER, 19));
        sickNote.setAubEndDate(LocalDate.of(2013, NOVEMBER, 20));
        validator.validate(sickNote, errors);
        verifyZeroInteractions(errors);
    }


    @Test
    public void ensureAUPeriodMustBeWithinSickNotePeriod() {

        sickNote.setAubStartDate(LocalDate.of(2013, DECEMBER, 19));
        sickNote.setAubEndDate(LocalDate.of(2013, DECEMBER, 20));
        validator.validate(sickNote, errors);
        verify(errors).rejectValue("aubStartDate", "sicknote.error.aubInvalidPeriod");
        verify(errors).rejectValue("aubEndDate", "sicknote.error.aubInvalidPeriod");
    }


    @Test
    public void ensureSickNoteMustNotHaveAnyOverlapping() {

        sickNote.setStartDate(LocalDate.of(2015, MARCH, 1));
        sickNote.setEndDate(LocalDate.of(2015, MARCH, 10));

        when(overlapService.checkOverlap(any(SickNote.class)))
            .thenReturn(OverlapCase.FULLY_OVERLAPPING);

        validator.validate(sickNote, errors);

        verify(errors).reject("application.error.overlap");
    }


    @Test
    public void ensureWorkingTimeConfigurationMustExistForPeriodOfSickNote() {

        LocalDate startDate = LocalDate.of(2015, MARCH, 1);
        LocalDate endDate = LocalDate.of(2015, MARCH, 10);

        sickNote.setStartDate(startDate);
        sickNote.setEndDate(endDate);

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class),
            any(LocalDate.class)))
            .thenReturn(Optional.empty());

        validator.validate(sickNote, errors);

        verify(workingTimeService).getByPersonAndValidityDateEqualsOrMinorDate(sickNote.getPerson(), startDate);
        verify(errors).reject("sicknote.error.noValidWorkingTime");
    }


    @Test
    public void ensureInvalidPeriodWithValidAUBPeriodIsNotValid() {

        LocalDate startDate = LocalDate.of(2016, MARCH, 16);
        LocalDate endDate = LocalDate.of(2016, MARCH, 14);

        LocalDate aubStartDate = LocalDate.of(2016, MARCH, 14);
        LocalDate aubEndDate = LocalDate.of(2016, MARCH, 16);

        sickNote.setStartDate(startDate);
        sickNote.setEndDate(endDate);

        sickNote.setAubStartDate(aubStartDate);
        sickNote.setAubEndDate(aubEndDate);

        validator.validate(sickNote, errors);

        verify(errors).rejectValue("endDate", "error.entry.invalidPeriod");
    }


    @Test
    public void ensureInvalidAUBPeriodWithValidPeriodIsNotValid() {

        LocalDate startDate = LocalDate.of(2016, MARCH, 14);
        LocalDate endDate = LocalDate.of(2016, MARCH, 16);

        LocalDate aubStartDate = LocalDate.of(2016, MARCH, 16);
        LocalDate aubEndDate = LocalDate.of(2016, MARCH, 14);

        sickNote.setStartDate(startDate);
        sickNote.setEndDate(endDate);

        sickNote.setAubStartDate(aubStartDate);
        sickNote.setAubEndDate(aubEndDate);

        validator.validate(sickNote, errors);

        verify(errors).rejectValue("aubEndDate", "error.entry.invalidPeriod");
    }
}
