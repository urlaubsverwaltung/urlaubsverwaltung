package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.overlap.OverlapService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentEntity;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import static java.time.Month.DECEMBER;
import static java.time.Month.MARCH;
import static java.time.Month.NOVEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createSickNote;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createWorkingTime;
import static org.synyx.urlaubsverwaltung.overlap.OverlapCase.FULLY_OVERLAPPING;
import static org.synyx.urlaubsverwaltung.overlap.OverlapCase.NO_OVERLAPPING;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;

/**
 * Unit test for {@link SickNoteValidator}.
 */
@ExtendWith(MockitoExtension.class)
class SickNoteValidatorTest {

    private SickNoteValidator sut;
    private final Clock clock = Clock.systemUTC();

    @Mock
    private OverlapService overlapService;
    @Mock
    private WorkingTimeService workingTimeService;

    @BeforeEach
    void setUp() {
        sut = new SickNoteValidator(overlapService, workingTimeService, Clock.systemUTC());
    }

    @Test
    void ensureValidDatesHaveNoErrors() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            LocalDate.of(2013, NOVEMBER, 19),
            LocalDate.of(2013, NOVEMBER, 20),
            FULL);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isZero();
    }

    @Test
    void ensureDayLengthMayNotBeNull() {

        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            LocalDate.of(2013, NOVEMBER, 19),
            LocalDate.of(2013, NOVEMBER, 20),
            null);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("dayLength").get(0).getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    void ensureStartDateMayNotBeNull() {
        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            null,
            LocalDate.of(2013, NOVEMBER, 20),
            FULL);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("startDate").get(0).getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    void ensureEndDateMayNotBeNull() {
        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            LocalDate.of(2013, NOVEMBER, 19),
            null,
            FULL);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("endDate").get(0).getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    void ensureStartDateMustBeBeforeEndDateToHaveAValidPeriod() {
        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            LocalDate.of(2013, DECEMBER, 10),
            LocalDate.of(2013, DECEMBER, 1),
            FULL);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("endDate").get(0).getCode()).isEqualTo("error.entry.invalidPeriod");

        verifyNoInteractions(overlapService, workingTimeService);
    }

    @Test
    void ensureStartAndEndDateMustBeEqualsDatesForDayLengthNoon() {
        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            LocalDate.of(2013, NOVEMBER, 19),
            LocalDate.of(2013, NOVEMBER, 21),
            NOON);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("endDate").get(0).getCode()).isEqualTo("sicknote.error.halfDayPeriod");
    }

    @Test
    void ensureStartAndEndDateMustBeEqualsDatesForDayLengthMorning() {
        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            LocalDate.of(2013, NOVEMBER, 19),
            LocalDate.of(2013, NOVEMBER, 21),
            MORNING);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("endDate").get(0).getCode()).isEqualTo("sicknote.error.halfDayPeriod");
    }

    @Test
    void ensureStartDateMustBeBeforeEndDateToHaveAValidPeriodForDayLengthMorning() {
        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            LocalDate.of(2013, NOVEMBER, 21),
            LocalDate.of(2013, NOVEMBER, 19),
            MORNING);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("endDate").get(0).getCode()).isEqualTo("error.entry.invalidPeriod");
    }

    @Test
    void ensureStartDateMustBeBeforeEndDateToHaveAValidPeriodForDayLengthNoon() {
        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            LocalDate.of(2013, NOVEMBER, 21),
            LocalDate.of(2013, NOVEMBER, 19),
            NOON);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("endDate").get(0).getCode()).isEqualTo("error.entry.invalidPeriod");
    }

    @Test
    void ensureCommentMayNotBeNull() {
        final SickNoteCommentEntity sickNoteCommentEntity = new SickNoteCommentEntity(clock);

        final Errors errors = new BeanPropertyBindingResult(sickNoteCommentEntity, "sickNote");
        sut.validateComment(sickNoteCommentEntity, errors);
        assertThat(errors.getFieldErrors("text").get(0).getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    void ensureTooLongCommentIsNotValid() {

        final SickNoteCommentEntity sickNoteCommentEntity = new SickNoteCommentEntity(clock);
        sickNoteCommentEntity.setText("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, "
            + "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, "
            + "sed diam voluptua. At vero eos et accusam et justo duo dolores bla bla");

        final Errors errors = new BeanPropertyBindingResult(sickNoteCommentEntity, "sickNote");
        sut.validateComment(sickNoteCommentEntity, errors);
        assertThat(errors.getFieldErrors("text").get(0).getCode()).isEqualTo("error.entry.tooManyChars");
    }

    @Test
    void ensureValidCommentHasNoErrors() {
        final SickNoteCommentEntity sickNoteCommentEntity = new SickNoteCommentEntity(clock);
        sickNoteCommentEntity.setText("I am a fluffy little comment");

        final Errors errors = new BeanPropertyBindingResult(sickNoteCommentEntity, "sickNote");
        sut.validateComment(sickNoteCommentEntity, errors);
        assertThat(errors.getErrorCount()).isZero();
    }

    @Test
    void ensureAUStartDateMustBeBeforeAUEndDateToHaveAValidPeriod() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
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
    void ensureValidAUPeriodHasNoErrors() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
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
    void ensureAUPeriodMustBeWithinSickNotePeriodMultipleDays() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            LocalDate.of(2013, NOVEMBER, 1),
            LocalDate.of(2013, NOVEMBER, 30),
            FULL);
        sickNote.setAubStartDate(LocalDate.of(2013, NOVEMBER, 1));
        sickNote.setAubEndDate(LocalDate.of(2013, NOVEMBER, 30));

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isZero();
    }

    @Test
    void ensureAUPeriodMustBeWithinSickNotePeriodMultipleDaysStart() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
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
    void ensureAUPeriodMustBeWithinSickNotePeriodMultipleDaysEnd() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            LocalDate.of(2013, NOVEMBER, 1),
            LocalDate.of(2013, NOVEMBER, 30),
            FULL);
        sickNote.setAubStartDate(LocalDate.of(2013, NOVEMBER, 20));
        sickNote.setAubEndDate(LocalDate.of(2013, NOVEMBER, 30));

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isZero();
    }

    @Test
    void ensureAUPeriodMustBeWithinSickNotePeriodMultipleDaysStartOverlapping() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            LocalDate.of(2013, NOVEMBER, 20),
            LocalDate.of(2013, NOVEMBER, 30),
            FULL);
        sickNote.setAubStartDate(LocalDate.of(2013, NOVEMBER, 1));
        sickNote.setAubEndDate(LocalDate.of(2013, NOVEMBER, 20));

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("aubStartDate").get(0).getCode()).isEqualTo("sicknote.error.aubInvalidPeriod");
    }

    @Test
    void ensureAUPeriodMustBeWithinSickNotePeriodMultipleDaysEndOverlapping() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            LocalDate.of(2013, NOVEMBER, 1),
            LocalDate.of(2013, NOVEMBER, 20),
            FULL);
        sickNote.setAubStartDate(LocalDate.of(2013, NOVEMBER, 20));
        sickNote.setAubEndDate(LocalDate.of(2013, NOVEMBER, 30));

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("aubEndDate").get(0).getCode()).isEqualTo("sicknote.error.aubInvalidPeriod");
    }

    @Test
    void ensureAUPeriodMustBeWithinSickNotePeriodMultipleDaysNoneOverlapping() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            LocalDate.of(2013, NOVEMBER, 10),
            LocalDate.of(2013, NOVEMBER, 20),
            FULL);
        sickNote.setAubStartDate(LocalDate.of(2013, NOVEMBER, 1));
        sickNote.setAubEndDate(LocalDate.of(2013, NOVEMBER, 9));

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("aubStartDate").get(0).getCode()).isEqualTo("sicknote.error.aubInvalidPeriod");
        assertThat(errors.getFieldErrors("aubEndDate").get(0).getCode()).isEqualTo("sicknote.error.aubInvalidPeriod");
    }

    @Test
    void ensureAUPeriodMustBeWithinSickNotePeriodOneDay() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));
        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            LocalDate.of(2013, NOVEMBER, 1),
            LocalDate.of(2013, NOVEMBER, 1),
            FULL);
        sickNote.setAubStartDate(LocalDate.of(2013, NOVEMBER, 1));
        sickNote.setAubEndDate(LocalDate.of(2013, NOVEMBER, 1));

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getErrorCount()).isZero();
    }

    @Test
    void ensureAUPeriodMustBeWithinSickNotePeriodButIsNotForOneDay() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            LocalDate.of(2013, NOVEMBER, 1),
            LocalDate.of(2013, NOVEMBER, 1),
            FULL);
        sickNote.setAubStartDate(LocalDate.of(2013, NOVEMBER, 2));
        sickNote.setAubEndDate(LocalDate.of(2013, NOVEMBER, 2));

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getFieldErrors("aubStartDate").get(0).getCode()).isEqualTo("sicknote.error.aubInvalidPeriod");
    }

    @Test
    void ensureSickNoteMustNotHaveAnyOverlapping() {

        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            LocalDate.of(2013, MARCH, 1),
            LocalDate.of(2013, MARCH, 10),
            FULL);
        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(FULLY_OVERLAPPING);

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getGlobalErrors().get(0).getCode()).isEqualTo("application.error.overlap");
    }

    @Test
    void ensureWorkingTimeConfigurationMustExistForPeriodOfSickNote() {
        final LocalDate startDate = LocalDate.of(2015, MARCH, 1);
        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
            startDate,
            LocalDate.of(2015, MARCH, 10),
            FULL);

        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.empty());

        final Errors errors = new BeanPropertyBindingResult(sickNote, "sickNote");
        sut.validate(sickNote, errors);
        assertThat(errors.getGlobalErrors().get(0).getCode()).isEqualTo("sicknote.error.noValidWorkingTime");
        verify(workingTimeService).getWorkingTime(sickNote.getPerson(), startDate);
    }

    @Test
    void ensureInvalidPeriodWithValidAUBPeriodIsNotValid() {
        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
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
    void ensureInvalidAUBPeriodWithValidPeriodIsNotValid() {

        when(overlapService.checkOverlap(any(SickNote.class))).thenReturn(NO_OVERLAPPING);
        when(workingTimeService.getWorkingTime(any(Person.class),
            any(LocalDate.class))).thenReturn(Optional.of(createWorkingTime()));

        final SickNote sickNote = createSickNote(new Person("muster", "Muster", "Marlene", "muster@example.org"),
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
