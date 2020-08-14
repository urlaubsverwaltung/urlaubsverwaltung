package org.synyx.urlaubsverwaltung.availability.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.DemoDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.settings.FederalState.BADEN_WUERTTEMBERG;

@ExtendWith(MockitoExtension.class)
class PublicHolidayAbsenceProviderTest {

    private PublicHolidayAbsenceProvider sut;

    @Mock
    private SickDayAbsenceProvider sickDayAbsenceProvider;
    @Mock
    private WorkingTimeService workingTimeService;
    @Mock
    private PublicHolidaysService publicHolidaysService;

    @BeforeEach
    void setUp() {
        sut = new PublicHolidayAbsenceProvider(sickDayAbsenceProvider, publicHolidaysService, workingTimeService);
    }

    @Test
    void ensurePersonIsNotAvailableOnHolidays() {

        final LocalDate newYearsDay = LocalDate.of(2016, 1, 1);
        when(publicHolidaysService.getWorkingDurationOfDate(newYearsDay, BADEN_WUERTTEMBERG)).thenReturn(new BigDecimal(0));

        final Person person = createPerson();
        when(workingTimeService.getFederalStateForPerson(person, newYearsDay)).thenReturn(BADEN_WUERTTEMBERG);

        final TimedAbsenceSpans emptyTimedAbsenceSpans = new TimedAbsenceSpans(new ArrayList<>());

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.addAbsence(emptyTimedAbsenceSpans, person, newYearsDay);
        final List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();
        assertThat(absencesList).hasSize(1);
        assertThat(absencesList.get(0).getPartOfDay()).isEqualTo(FULL.name());
        assertThat(absencesList.get(0).getRatio()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void ensurePersonIsHalfAvailableOnHolidays() {

        final LocalDate newYearsDay = LocalDate.of(2016, 1, 1);
        when(publicHolidaysService.getWorkingDurationOfDate(newYearsDay, BADEN_WUERTTEMBERG)).thenReturn(BigDecimal.valueOf(0.5));

        final Person person = createPerson();
        when(workingTimeService.getFederalStateForPerson(person, newYearsDay)).thenReturn(BADEN_WUERTTEMBERG);

        final TimedAbsenceSpans emptyTimedAbsenceSpans = new TimedAbsenceSpans(new ArrayList<>());

        final TimedAbsenceSpans updatedTimedAbsenceSpans = sut.addAbsence(emptyTimedAbsenceSpans, person, newYearsDay);
        final List<TimedAbsence> absencesList = updatedTimedAbsenceSpans.getAbsencesList();
        assertThat(absencesList).hasSize(1);
        assertThat(absencesList.get(0).getPartOfDay()).isEqualTo(NOON.name());
        assertThat(absencesList.get(0).getRatio()).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void ensureDoesNotCallNextProviderIfAlreadyAbsentForWholeDay() {

        final LocalDate newYearsDay = LocalDate.of(2016, 1, 1);
        when(publicHolidaysService.getWorkingDurationOfDate(newYearsDay, BADEN_WUERTTEMBERG)).thenReturn(new BigDecimal(0));

        final Person person = createPerson();
        when(workingTimeService.getFederalStateForPerson(person, newYearsDay)).thenReturn(BADEN_WUERTTEMBERG);

        final TimedAbsenceSpans emptyTimedAbsenceSpans = new TimedAbsenceSpans(new ArrayList<>());

        sut.checkForAbsence(emptyTimedAbsenceSpans, person, newYearsDay);
        verifyNoMoreInteractions(sickDayAbsenceProvider);
    }

    @Test
    void ensureCallsNextSickDayAbsenceProviderIfNotAbsentForHoliday() {

        final LocalDate standardWorkingDay = LocalDate.of(2016, 1, 4);
        when(publicHolidaysService.getWorkingDurationOfDate(standardWorkingDay, BADEN_WUERTTEMBERG)).thenReturn(new BigDecimal(1));

        final Person person = createPerson();
        when(workingTimeService.getFederalStateForPerson(person, standardWorkingDay)).thenReturn(BADEN_WUERTTEMBERG);

        final TimedAbsenceSpans emptyTimedAbsenceSpans = new TimedAbsenceSpans(new ArrayList<>());

        sut.checkForAbsence(emptyTimedAbsenceSpans, person, standardWorkingDay);
        verify(sickDayAbsenceProvider).checkForAbsence(emptyTimedAbsenceSpans, person, standardWorkingDay);
    }
}
