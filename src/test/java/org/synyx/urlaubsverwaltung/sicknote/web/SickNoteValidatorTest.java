package org.synyx.urlaubsverwaltung.sicknote.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteComment;
import org.synyx.urlaubsverwaltung.workingtime.OverlapService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.LocalDate;
import java.util.Optional;

import static java.time.Month.DECEMBER;
import static java.time.Month.MARCH;
import static java.time.Month.NOVEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createSickNote;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createWorkingTime;
import static org.synyx.urlaubsverwaltung.workingtime.OverlapCase.FULLY_OVERLAPPING;
import static org.synyx.urlaubsverwaltung.workingtime.OverlapCase.NO_OVERLAPPING;


/**
 * Unit test for {@link SickNoteValidator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SickNoteValidatorTest {

    private SickNoteValidator sut;

    @Mock
    private OverlapService overlapService;
    @Mock
    private WorkingTimeService workingTimeService;
    @Mock
    private Errors errors;

    @Before
    public void setUp() {

        sut = new SickNoteValidator(overlapService, workingTimeService);

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
    }

    @Test
    public void ensureValidDatesHaveNoErrors() {

        final SickNote sickNote = createSickNote(createPerson(),
            LocalDate.of(2013, NOVEMBER, 19),
            LocalDate.of(2013, NOVEMBER, 20),
            FULL);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isZero();
    }

    @Test
    public void ensureDayLengthMayNotBeNull() {

        final SickNote sickNote = createSickNote(createPerson(),
            LocalDate.of(2013, NOVEMBER, 19),
            LocalDate.of(2013, NOVEMBER, 20),
            null);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("dayLength").get(0).getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    public void ensureStartDateMayNotBeNull() {
        final SickNote sickNote = createSickNote(createPerson(),
            null,
            LocalDate.of(2013, NOVEMBER, 20),
            FULL);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("startDate").get(0).getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    public void ensureEndDateMayNotBeNull() {
        final SickNote sickNote = createSickNote(createPerson(),
            LocalDate.of(2013, NOVEMBER, 19),
            null,
            FULL);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("endDate").get(0).getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    public void ensureStartDateMustBeBeforeEndDateToHaveAValidPeriod() {
        final SickNote sickNote = createSickNote(createPerson(),
            LocalDate.of(2013, DECEMBER, 10),
            LocalDate.of(2013, DECEMBER, 1),
            FULL);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("endDate").get(0).getCode()).isEqualTo("error.entry.invalidPeriod");
    }

    @Test
    public void ensureStartAndEndDateMustBeEqualsDatesForDayLengthNoon() {
        final SickNote sickNote = createSickNote(createPerson(),
            LocalDate.of(2013, NOVEMBER, 19),
            LocalDate.of(2013, NOVEMBER, 21),
            NOON);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("endDate").get(0).getCode()).isEqualTo("sicknote.error.halfDayPeriod");
    }

    @Test
    public void ensureStartAndEndDateMustBeEqualsDatesForDayLengthMorning() {
        final SickNote sickNote = createSickNote(createPerson(),
            LocalDate.of(2013, NOVEMBER, 19),
            LocalDate.of(2013, NOVEMBER, 21),
            MORNING);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("endDate").get(0).getCode()).isEqualTo("sicknote.error.halfDayPeriod");
    }

    @Test
    public void ensureStartDateMustBeBeforeEndDateToHaveAValidPeriodForDayLengthMorning() {
        final SickNote sickNote = createSickNote(createPerson(),
            LocalDate.of(2013, NOVEMBER, 21),
            LocalDate.of(2013, NOVEMBER, 19),
            MORNING);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("endDate").get(0).getCode()).isEqualTo("error.entry.invalidPeriod");
    }

    @Test
    public void ensureStartDateMustBeBeforeEndDateToHaveAValidPeriodForDayLengthNoon() {
        final SickNote sickNote = createSickNote(createPerson(),
            LocalDate.of(2013, NOVEMBER, 21),
            LocalDate.of(2013, NOVEMBER, 19),
            NOON);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("endDate").get(0).getCode()).isEqualTo("error.entry.invalidPeriod");
    }

    @Test
    public void ensureCommentMayNotBeNull() {
        final SickNoteComment sickNoteComment = new SickNoteComment();

        final Errors errors = new BeanPropertyBindingResult(sickNoteComment, "sickNote");
        sut.validateComment(sickNoteComment, errors);
        assertThat(errors.getFieldErrors("text").get(0).getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    public void ensureTooLongCommentIsNotValid() {

        final SickNoteComment sickNoteComment = new SickNoteComment();
        sickNoteComment.setText("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, "
            + "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, "
            + "sed diam voluptua. At vero eos et accusam et justo duo dolores bla bla");

        final Errors errors = new BeanPropertyBindingResult(sickNoteComment, "sickNote");
        sut.validateComment(sickNoteComment, errors);
        assertThat(errors.getFieldErrors("text").get(0).getCode()).isEqualTo("error.entry.tooManyChars");
    }

    @Test
    public void ensureValidCommentHasNoErrors() {
        final SickNoteComment sickNoteComment = new SickNoteComment();
        sickNoteComment.setText("I am a fluffy little comment");

        final Errors errors = new BeanPropertyBindingResult(sickNoteComment, "sickNote");
        sut.validateComment(sickNoteComment, errors);
        assertThat(errors.getErrorCount()).isZero();
    }

    @Test
    public void ensureAUStartDateMustBeBeforeAUEndDateToHaveAValidPeriod() {
        final SickNote sickNote = createSickNote(createPerson(),
            LocalDate.of(2013, NOVEMBER, 1),
            LocalDate.of(2013, NOVEMBER, 30),
            FULL);
        sickNote.setAubStartDate(LocalDate.of(2013, NOVEMBER, 20));
        sickNote.setAubEndDate(LocalDate.of(2013, NOVEMBER, 19));

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("aubEndDate").get(0).getCode()).isEqualTo("error.entry.invalidPeriod");
    }

    @Test
    public void ensureValidAUPeriodHasNoErrors() {
        final SickNote sickNote = createSickNote(createPerson(),
            LocalDate.of(2013, NOVEMBER, 1),
            LocalDate.of(2013, NOVEMBER, 30),
            FULL);
        sickNote.setAubStartDate(LocalDate.of(2013, NOVEMBER, 19));
        sickNote.setAubEndDate(LocalDate.of(2013, NOVEMBER, 20));

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isZero();
    }

    @Test
    public void ensureAUPeriodMustBeWithinSickNotePeriod() {
        final SickNote sickNote = createSickNote(createPerson(),
            LocalDate.of(2013, NOVEMBER, 1),
            LocalDate.of(2013, NOVEMBER, 30),
            FULL);
        sickNote.setAubStartDate(LocalDate.of(2013, NOVEMBER, 1));
        sickNote.setAubEndDate(LocalDate.of(2013, NOVEMBER, 10));

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isZero();
    }

    @Test
    public void ensureSickNoteMustNotHaveAnyOverlapping() {
        final SickNote sickNote = createSickNote(createPerson(),
            LocalDate.of(2013, MARCH, 1),
            LocalDate.of(2013, MARCH, 10),
            FULL);
        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(FULLY_OVERLAPPING);

        sut.validate(sickNote, errors);

        verify(errors).reject("application.error.overlap");
    }

    @Test
    public void ensureWorkingTimeConfigurationMustExistForPeriodOfSickNote() {
        final LocalDate startDate = LocalDate.of(2015, MARCH, 1);
        final SickNote sickNote = createSickNote(createPerson(),
            startDate,
            LocalDate.of(2015, MARCH, 10),
            FULL);

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.empty());

        sut.validate(sickNote, errors);

        verify(workingTimeService).getByPersonAndValidityDateEqualsOrMinorDate(sickNote.getPerson(), startDate);
        verify(errors).reject("sicknote.error.noValidWorkingTime");
    }

    @Test
    public void ensureInvalidPeriodWithValidAUBPeriodIsNotValid() {
        final SickNote sickNote = createSickNote(createPerson(),
            LocalDate.of(2013, NOVEMBER, 10),
            LocalDate.of(2013, NOVEMBER, 4),
            FULL);
        sickNote.setAubStartDate(LocalDate.of(2013, NOVEMBER, 1));
        sickNote.setAubEndDate(LocalDate.of(2013, NOVEMBER, 2));

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("endDate").get(0).getCode()).isEqualTo("error.entry.invalidPeriod");
    }

    @Test
    public void ensureInvalidAUBPeriodWithValidPeriodIsNotValid() {
        final SickNote sickNote = createSickNote(createPerson(),
            LocalDate.of(2013, NOVEMBER, 1),
            LocalDate.of(2013, NOVEMBER, 4),
            FULL);
        sickNote.setAubStartDate(LocalDate.of(2013, NOVEMBER, 2));
        sickNote.setAubEndDate(LocalDate.of(2013, NOVEMBER, 1));

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("aubEndDate").get(0).getCode()).isEqualTo("error.entry.invalidPeriod");
    }
}
