package org.synyx.urlaubsverwaltung.availability.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createSickNote;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus.CANCELLED;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus.CONVERTED_TO_VACATION;


@ExtendWith(MockitoExtension.class)
class SickDayAbsenceProviderTest {

    private SickDayAbsenceProvider sut;

    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private VacationAbsenceProvider nextAbsenceProvider;

    @BeforeEach
    void setUp() {
        sut = new SickDayAbsenceProvider(nextAbsenceProvider, sickNoteService);
    }

    @Test
    void ensurePersonIsNotAvailableOnFullSickDay() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final TimedAbsenceSpans knownAbsences = new TimedAbsenceSpans(new ArrayList<>());
        final LocalDate sickDay = LocalDate.of(2016, 1, 4);

        final SickNote sickNote = createSickNote(person, sickDay, sickDay, FULL);
        when(sickNoteService.getByPersonAndPeriod(person, sickDay, sickDay)).thenReturn(singletonList(sickNote));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(knownAbsences, person, sickDay);
        final List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();
        assertThat(absencesList).hasSize(1);
        assertThat(absencesList.get(0).getPartOfDay()).isEqualTo(FULL.name());
        assertThat(absencesList.get(0).getRatio()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    void ensurePersonIsNotAvailableOnTwoHalfSickDays() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final TimedAbsenceSpans knownAbsences = new TimedAbsenceSpans(new ArrayList<>());
        final LocalDate sickDay = LocalDate.of(2016, 1, 4);

        final SickNote sickNoteMorning = createSickNote(person, sickDay, sickDay, MORNING);
        final SickNote sickNoteNoon = createSickNote(person, sickDay, sickDay, NOON);
        when(sickNoteService.getByPersonAndPeriod(person, sickDay, sickDay)).thenReturn(List.of(sickNoteMorning, sickNoteNoon));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(knownAbsences, person, sickDay);
        final List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();
        assertThat(absencesList).hasSize(2);
        assertThat(absencesList.get(0).getPartOfDay()).isEqualTo(MORNING.name());
        assertThat(absencesList.get(0).getRatio()).isEqualTo(BigDecimal.valueOf(0.5));
        assertThat(absencesList.get(1).getPartOfDay()).isEqualTo(NOON.name());
        assertThat(absencesList.get(1).getRatio()).isEqualTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void ensurePersonIsAvailableOnFullSickDayCancelled() {

        // we need this because sick day provider is not the last provider and otherwise the mock
        // would return null
        when(nextAbsenceProvider.checkForAbsence(any(), any(), any())).then(returnsFirstArg());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final TimedAbsenceSpans knownAbsences = new TimedAbsenceSpans(new ArrayList<>());
        final LocalDate sickDay = LocalDate.of(2016, 1, 4);

        final SickNote sickNote = createSickNote(person, sickDay, sickDay, FULL);
        sickNote.setStatus(CANCELLED);
        when(sickNoteService.getByPersonAndPeriod(person, sickDay, sickDay)).thenReturn(singletonList(sickNote));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(knownAbsences, person, sickDay);
        final List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();
        assertThat(absencesList).isEmpty();
    }

    @Test
    void ensurePersonIsAvailableOnFullSickDayConvertedToVacation() {

        // we need this because sick day provider is not the last provider and otherwise the mock
        // would return null
        when(nextAbsenceProvider.checkForAbsence(any(), any(), any())).then(returnsFirstArg());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final TimedAbsenceSpans knownAbsences = new TimedAbsenceSpans(new ArrayList<>());
        final LocalDate sickDay = LocalDate.of(2016, 1, 4);

        final SickNote sickNote = createSickNote(person, sickDay, sickDay, FULL);
        sickNote.setStatus(CONVERTED_TO_VACATION);
        when(sickNoteService.getByPersonAndPeriod(person, sickDay, sickDay)).thenReturn(singletonList(sickNote));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(knownAbsences, person, sickDay);
        final List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();
        assertThat(absencesList).isEmpty();
    }

    @Test
    void ensureDoesNotCallNextProviderIfAlreadyAbsentForWholeDay() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate sickDay = LocalDate.of(2016, 1, 4);

        final SickNote sickNote = createSickNote(person, sickDay, sickDay, FULL);
        when(sickNoteService.getByPersonAndPeriod(person, sickDay, sickDay)).thenReturn(singletonList(sickNote));

        sut.checkForAbsence(new TimedAbsenceSpans(new ArrayList<>()), person, sickDay);

        verifyNoMoreInteractions(nextAbsenceProvider);
    }

    @Test
    void ensureCallsVacationAbsenceProviderIfNotAbsentForSickDay() {

        final LocalDate sickDay = LocalDate.of(2016, 1, 5);
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final TimedAbsenceSpans knownAbsences = new TimedAbsenceSpans(new ArrayList<>());
        sut.checkForAbsence(knownAbsences, person, sickDay);

        verify(nextAbsenceProvider).checkForAbsence(knownAbsences, person, sickDay);
    }
}
