package org.synyx.urlaubsverwaltung.availability.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
import static org.synyx.urlaubsverwaltung.availability.api.TimedAbsence.Type.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus.CANCELLED;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus.CONVERTED_TO_VACATION;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createSickNote;


@RunWith(MockitoJUnitRunner.class)
public class SickDayAbsenceProviderTest {

    private SickDayAbsenceProvider sut;

    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private VacationAbsenceProvider nextAbsenceProvider;

    @Before
    public void setUp() {
        sut = new SickDayAbsenceProvider(nextAbsenceProvider, sickNoteService);
    }

    @Test
    public void ensurePersonIsNotAvailableOnFullSickDay() {

        final Person person = createPerson();
        final TimedAbsenceSpans knownAbsences = new TimedAbsenceSpans(new ArrayList<>());
        final LocalDate sickDay = LocalDate.of(2016, 1, 4);

        final SickNote sickNote = createSickNote(person, sickDay, sickDay, FULL);
        when(sickNoteService.getByPersonAndPeriod(person, sickDay, sickDay)).thenReturn(singletonList(sickNote));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(knownAbsences, person, sickDay);
        final List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();
        assertThat(absencesList).hasSize(1);
        assertThat(absencesList.get(0).getType()).isEqualTo(SICK_NOTE);
        assertThat(absencesList.get(0).getPartOfDay()).isEqualTo(FULL.name());
        assertThat(absencesList.get(0).getRatio()).isEqualTo(BigDecimal.valueOf(1.0));
    }

    @Test
    public void ensurePersonIsNotAvailableOnTwoHalfSickDays() {

        final Person person = createPerson();
        final TimedAbsenceSpans knownAbsences = new TimedAbsenceSpans(new ArrayList<>());
        final LocalDate sickDay = LocalDate.of(2016, 1, 4);

        final SickNote sickNoteMorning = createSickNote(person, sickDay, sickDay, MORNING);
        final SickNote sickNoteNoon = createSickNote(person, sickDay, sickDay, NOON);
        when(sickNoteService.getByPersonAndPeriod(person, sickDay, sickDay)).thenReturn(List.of(sickNoteMorning, sickNoteNoon));

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.checkForAbsence(knownAbsences, person, sickDay);
        final List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();
        assertThat(absencesList).hasSize(2);
        assertThat(absencesList.get(0).getType()).isEqualTo(SICK_NOTE);
        assertThat(absencesList.get(0).getPartOfDay()).isEqualTo(MORNING.name());
        assertThat(absencesList.get(0).getRatio()).isEqualTo(BigDecimal.valueOf(0.5));
        assertThat(absencesList.get(1).getType()).isEqualTo(SICK_NOTE);
        assertThat(absencesList.get(1).getPartOfDay()).isEqualTo(NOON.name());
        assertThat(absencesList.get(1).getRatio()).isEqualTo(BigDecimal.valueOf(0.5));
    }

    @Test
    public void ensurePersonIsAvailableOnFullSickDayCancelled() {

        // we need this because sick day provider is not the last provider and otherwise the mock
        // would return null
        when(nextAbsenceProvider.checkForAbsence(any(), any(), any())).then(returnsFirstArg());

        final Person person = createPerson();
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
    public void ensurePersonIsAvailableOnFullSickDayConvertedToVacation() {

        // we need this because sick day provider is not the last provider and otherwise the mock
        // would return null
        when(nextAbsenceProvider.checkForAbsence(any(), any(), any())).then(returnsFirstArg());

        final Person person = createPerson();
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
    public void ensureDoesNotCallNextProviderIfAlreadyAbsentForWholeDay() {

        final Person person = createPerson();
        final LocalDate sickDay = LocalDate.of(2016, 1, 4);

        final SickNote sickNote = createSickNote(person, sickDay, sickDay, FULL);
        when(sickNoteService.getByPersonAndPeriod(person, sickDay, sickDay)).thenReturn(singletonList(sickNote));

        sut.checkForAbsence(new TimedAbsenceSpans(new ArrayList<>()), person, sickDay);

        verifyNoMoreInteractions(nextAbsenceProvider);
    }

    @Test
    public void ensureCallsVacationAbsenceProviderIfNotAbsentForSickDay() {

        final LocalDate sickDay = LocalDate.of(2016, 1, 5);
        final Person person = createPerson();
        final TimedAbsenceSpans knownAbsences = new TimedAbsenceSpans(new ArrayList<>());
        sut.checkForAbsence(knownAbsences, person, sickDay);

        verify(nextAbsenceProvider).checkForAbsence(knownAbsences, person, sickDay);
    }
}
