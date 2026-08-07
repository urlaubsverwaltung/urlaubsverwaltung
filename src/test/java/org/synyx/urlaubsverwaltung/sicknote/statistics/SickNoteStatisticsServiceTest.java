package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.valueOf;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.OCTOBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarFactory.workingTimeCalendarMondayToSunday;

@ExtendWith(MockitoExtension.class)
class SickNoteStatisticsServiceTest {

    private SickNoteStatisticsService sut;

    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private SickNoteRelevantPersonsService sickNoteRelevantPersonsService;
    @Mock
    private WorkingTimeCalendarService workingTimeCalendarService;

    private static final Clock clock = Clock.systemDefaultZone();

    @BeforeEach
    void setUp() {
        sut = new SickNoteStatisticsService(sickNoteService, sickNoteRelevantPersonsService, workingTimeCalendarService, clock);
    }

    @Test
    void ensureCreateStatisticsWithoutRelevantPersons() {

        final Person person = new Person();
        person.setId(1L);

        final LocalDate from = LocalDate.of(2026, JANUARY, 1);
        final LocalDate to = LocalDate.of(2026, DECEMBER, 31);

        when(sickNoteRelevantPersonsService.getStatisticRelevantPersons(from, to, person)).thenReturn(List.of());

        final SickNoteStatistics sickNoteStatistics = sut.createStatisticsForPerson(from, to, person);
        assertThat(sickNoteStatistics.getTotalNumberOfSickNotes()).isZero();
        assertThat(sickNoteStatistics.getNumberOfPersonsWithMinimumOneSickNote()).isZero();

        verifyNoInteractions(sickNoteService);
        verifyNoInteractions(workingTimeCalendarService);
    }

    @Test
    void ensureCreateStatisticsForRelevantPersons() {

        final Year year = Year.of(2022);

        final Person office = new Person();
        office.setId(1L);

        final Person person = new Person();
        person.setId(2L);

        final List<Person> relevantPersons = List.of(office, person);

        final LocalDate from = LocalDate.of(2022, JANUARY, 1);
        final LocalDate to = LocalDate.of(2022, DECEMBER, 31);
        final LocalDate sickNoteDate = LocalDate.of(2022, OCTOBER, 10);

        when(sickNoteRelevantPersonsService.getStatisticRelevantPersons(from, to, office)).thenReturn(relevantPersons);

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .sickNoteType(sickNoteType(SickNoteCategory.SICK_NOTE))
            .startDate(sickNoteDate)
            .endDate(sickNoteDate)
            .dayLength(FULL)
            .workingTimeCalendar(workingTimeCalendarMondayToSunday(from, to))
            .build();
        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), relevantPersons, from, to)).thenReturn(List.of(sickNote));

        final SickNoteStatistics sickNoteStatistics = sut.createStatisticsForPerson(from, to, office);
        assertThat(sickNoteStatistics.getTotalNumberOfSickNotes()).isOne();
        assertThat(sickNoteStatistics.getNumberOfPersonsWithMinimumOneSickNote()).isOne();

        verify(workingTimeCalendarService).getWorkingTimesByPersons(relevantPersons, year);
    }

    @Test
    void ensureCreateStatisticsForPersonUsesWorkingTimeCalendarsForSickRate() {

        final Year year = Year.of(2022);

        final Person office = new Person();
        office.setId(1L);

        final Person person = new Person();
        person.setId(2L);

        final List<Person> relevantPersons = List.of(office, person);

        final LocalDate from = LocalDate.of(2022, JANUARY, 1);
        final LocalDate to = LocalDate.of(2022, DECEMBER, 31);
        final LocalDate sickNoteStart = LocalDate.of(2022, JANUARY, 10);
        final LocalDate sickNoteEnd = LocalDate.of(2022, JANUARY, 11);

        when(sickNoteRelevantPersonsService.getStatisticRelevantPersons(from, to, office)).thenReturn(relevantPersons);

        // only the two sick note dates are target work days for `person`, `office` has none at all
        // --> target work days in January == sick days in January
        final WorkingTimeCalendar personCalendar = workingTimeCalendarMondayToSunday(from, to,
            date -> date.equals(sickNoteStart) || date.equals(sickNoteEnd));
        final WorkingTimeCalendar officeCalendar = workingTimeCalendarMondayToSunday(from, to, date -> false);

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .sickNoteType(sickNoteType(SickNoteCategory.SICK_NOTE))
            .startDate(sickNoteStart)
            .endDate(sickNoteEnd)
            .dayLength(FULL)
            .workingTimeCalendar(personCalendar)
            .build();
        when(sickNoteService.getForStatesAndPerson(List.of(ACTIVE), relevantPersons, from, to)).thenReturn(List.of(sickNote));
        when(workingTimeCalendarService.getWorkingTimesByPersons(relevantPersons, year))
            .thenReturn(Map.of(office, officeCalendar, person, personCalendar));

        final SickNoteStatistics sickNoteStatistics = sut.createStatisticsForPerson(from, to, office);

        verify(workingTimeCalendarService).getWorkingTimesByPersons(relevantPersons, year);
        assertThat(sickNoteStatistics.getSickRateByMonth().getFirst()).isEqualByComparingTo(valueOf(100));
    }

    private static SickNoteType sickNoteType(SickNoteCategory category) {
        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setId(1L);
        sickNoteType.setCategory(category);
        return sickNoteType;
    }
}
