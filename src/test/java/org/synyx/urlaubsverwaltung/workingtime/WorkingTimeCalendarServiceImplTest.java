package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHoliday;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.time.Month.APRIL;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.JULY;
import static java.time.Month.JUNE;
import static java.time.Month.MARCH;
import static java.time.Month.OCTOBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BERLIN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_GENF;

@ExtendWith(MockitoExtension.class)
class WorkingTimeCalendarServiceImplTest {

    private WorkingTimeCalendarServiceImpl sut;

    @Mock
    private WorkingTimeRepository workingTimeRepository;
    @Mock
    private PublicHolidaysService publicHolidaysService;
    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        sut = new WorkingTimeCalendarServiceImpl(workingTimeRepository, publicHolidaysService, settingsService);
    }

    @Test
    void ensureGetNextWorkingDayFollowingTo() {

        final Person person = new Person();
        person.setId(1L);

        final WorkingTimeEntity workingTimeEntity = createWorkingTimeEntity(person, LocalDate.of(2024, JANUARY, 1),
            FULL, FULL, ZERO, FULL, FULL, ZERO, ZERO, GERMANY_BADEN_WUERTTEMBERG);

        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(List.of(person)))
            .thenReturn(List.of(workingTimeEntity));

        final Function<LocalDate, Optional<LocalDate>> get =
            date -> sut.getNextWorkingDayFollowingTo(person, date);

        // first day of valid(From) workingTime
        assertThat(get.apply(LocalDate.of(2024, JANUARY, 1))).hasValue(LocalDate.of(2024, JANUARY, 2));

        // within valid working time
        assertThat(get.apply(LocalDate.of(2024, JULY, 15))).hasValue(LocalDate.of(2024, JULY, 16));
        assertThat(get.apply(LocalDate.of(2024, JULY, 16))).hasValue(LocalDate.of(2024, JULY, 18));
        assertThat(get.apply(LocalDate.of(2024, JULY, 17))).hasValue(LocalDate.of(2024, JULY, 18));
        assertThat(get.apply(LocalDate.of(2024, JULY, 18))).hasValue(LocalDate.of(2024, JULY, 19));
        assertThat(get.apply(LocalDate.of(2024, JULY, 19))).hasValue(LocalDate.of(2024, JULY, 22));
        assertThat(get.apply(LocalDate.of(2024, JULY, 20))).hasValue(LocalDate.of(2024, JULY, 22));
        assertThat(get.apply(LocalDate.of(2024, JULY, 21))).hasValue(LocalDate.of(2024, JULY, 22));

        // future
        assertThat(get.apply(LocalDate.of(2024, DECEMBER, 31))).hasValue(LocalDate.of(2025, JANUARY, 2));
        assertThat(get.apply(LocalDate.of(2025, JANUARY, 1))).hasValue(LocalDate.of(2025, JANUARY, 2));
    }

    @Test
    void ensureGetNextWorkingDayFollowingToReturnsDateOfFutureWorkingTime() {

        final Person person = new Person();
        person.setId(1L);

        final WorkingTimeEntity workingTimeEntity = createWorkingTimeEntity(person, LocalDate.of(2024, JANUARY, 1),
            FULL, FULL, ZERO, FULL, FULL, ZERO, ZERO, GERMANY_BADEN_WUERTTEMBERG);

        final WorkingTimeEntity workingTimeEntityValidFromJuly = createWorkingTimeEntity(person, LocalDate.of(2024, JULY, 1),
            ZERO, ZERO, ZERO, ZERO, FULL, FULL, FULL, GERMANY_BADEN_WUERTTEMBERG);

        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(List.of(person)))
            // note that returned list has to be ordered by validFrom!
            .thenReturn(List.of(workingTimeEntityValidFromJuly, workingTimeEntity));

        final Function<LocalDate, Optional<LocalDate>> get =
            date -> sut.getNextWorkingDayFollowingTo(person, date);

        // outside of future workingTime, but next working day is within future workingTime
        assertThat(get.apply(LocalDate.of(2024, JUNE, 30))).hasValue(LocalDate.of(2024, JULY, 5));

        // within valid working time
        assertThat(get.apply(LocalDate.of(2024, JULY, 1))).hasValue(LocalDate.of(2024, JULY, 5));
        assertThat(get.apply(LocalDate.of(2024, JULY, 2))).hasValue(LocalDate.of(2024, JULY, 5));
        assertThat(get.apply(LocalDate.of(2024, JULY, 3))).hasValue(LocalDate.of(2024, JULY, 5));
        assertThat(get.apply(LocalDate.of(2024, JULY, 4))).hasValue(LocalDate.of(2024, JULY, 5));
        assertThat(get.apply(LocalDate.of(2024, JULY, 5))).hasValue(LocalDate.of(2024, JULY, 6));
        assertThat(get.apply(LocalDate.of(2024, JULY, 6))).hasValue(LocalDate.of(2024, JULY, 7));
        assertThat(get.apply(LocalDate.of(2024, JULY, 7))).hasValue(LocalDate.of(2024, JULY, 12));
    }

    @Test
    void ensureGetNextWorkingDayFollowingToReturnsEmptyOptionalWhenEmptyWorkingTimes() {

        final Person person = new Person();
        person.setId(1L);

        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(List.of(person)))
            .thenReturn(List.of());

        final Optional<LocalDate> actual = sut.getNextWorkingDayFollowingTo(person, LocalDate.of(2024, JULY, 21));
        assertThat(actual).isEmpty();
    }

    @Test
    void ensureGetNextWorkingDayFollowingToReturnsEmptyOptionalWhenThereAreNoWorkingTimesPresentInThePast() {

        final Person person = new Person();
        person.setId(1L);

        final WorkingTimeEntity workingTimeEntity = createWorkingTimeEntity(person, LocalDate.of(2024, JANUARY, 1),
            FULL, FULL, ZERO, FULL, FULL, ZERO, ZERO, GERMANY_BADEN_WUERTTEMBERG);

        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(List.of(person)))
            .thenReturn(List.of(workingTimeEntity));

        final Function<LocalDate, Optional<LocalDate>> get =
            date -> sut.getNextWorkingDayFollowingTo(person, date);

        assertThat(get.apply(LocalDate.of(2023, DECEMBER, 30))).isEmpty();

        // is present because the next day matches a workingTime
        assertThat(get.apply(LocalDate.of(2023, DECEMBER, 31))).isPresent();
    }

    @Test
    void ensureGetNextWorkingDayFollowingToDoesNotThrowStackoverflowForWorkingTimeWithoutWorkingDays() {

        final Person person = new Person();
        person.setId(1L);

        final WorkingTimeEntity workingTimeEntity = createWorkingTimeEntity(person, LocalDate.of(2024, JANUARY, 1),
            ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, GERMANY_BADEN_WUERTTEMBERG);

        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(List.of(person)))
            .thenReturn(List.of(workingTimeEntity));

        assertThat(sut.getNextWorkingDayFollowingTo(person, LocalDate.of(2024, JULY, 21))).isEmpty();
    }

    @Test
    void ensureGetWorkingTimesByPersonsAndYear() {
        final Person person = new Person();
        person.setId(1L);

        final Person person2 = new Person();
        person2.setId(2L);

        final List<Person> persons = List.of(person, person2);
        final DateRange dateRange = new DateRange(LocalDate.of(2022, JANUARY, 1), LocalDate.of(2022, DECEMBER, 31));

        final WorkingTimeEntity workingTimeEntity = createWorkingTimeEntity(person, LocalDate.of(2022, JANUARY, 1),
            FULL, FULL, FULL, FULL, FULL, FULL, FULL, GERMANY_BADEN_WUERTTEMBERG);

        final WorkingTimeEntity workingTimeEntity2 = createWorkingTimeEntity(person2, LocalDate.of(2022, JANUARY, 1),
            DayLength.NOON, DayLength.NOON, DayLength.NOON, DayLength.NOON, DayLength.NOON, DayLength.NOON, DayLength.NOON, GERMANY_BADEN_WUERTTEMBERG);

        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(persons))
            .thenReturn(List.of(workingTimeEntity, workingTimeEntity2));

        final Map<Person, WorkingTimeCalendar> actual = sut.getWorkingTimesByPersons(persons, Year.of(2022));
        assertThat(actual)
            .hasSize(2)
            .containsKeys(person, person2);

        final WorkingTimeCalendar personWorkingTimeCalendar = actual.get(person);
        for (LocalDate date : dateRange) {
            assertThat(personWorkingTimeCalendar.workingTime(date)).hasValue(BigDecimal.ONE);
        }

        final WorkingTimeCalendar person2WorkingTimeCalendar = actual.get(person2);
        for (LocalDate date : dateRange) {
            assertThat(person2WorkingTimeCalendar.workingTime(date)).hasValue(BigDecimal.valueOf(0.5));
        }
    }

    @Test
    void ensureGetWorkingTimesByPersonsAndPeriod() {
        final Person person = new Person();
        person.setId(1L);

        final Person person2 = new Person();
        person2.setId(2L);

        final List<Person> persons = List.of(person, person2);
        final DateRange dateRange = new DateRange(LocalDate.of(2022, JUNE, 1), LocalDate.of(2022, JUNE, 30));

        final WorkingTimeEntity workingTimeEntity = createWorkingTimeEntity(person, LocalDate.of(2022, JUNE, 1),
            FULL, FULL, FULL, FULL, FULL, FULL, FULL, GERMANY_BADEN_WUERTTEMBERG);

        final WorkingTimeEntity workingTimeEntity2 = createWorkingTimeEntity(person2, LocalDate.of(2022, JUNE, 1),
            DayLength.NOON, DayLength.NOON, DayLength.NOON, DayLength.NOON, DayLength.NOON, DayLength.NOON, DayLength.NOON, GERMANY_BADEN_WUERTTEMBERG);

        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(persons))
            .thenReturn(List.of(workingTimeEntity, workingTimeEntity2));

        final Map<Person, WorkingTimeCalendar> actual = sut.getWorkingTimesByPersons(persons, dateRange);
        assertThat(actual)
            .hasSize(2)
            .containsKeys(person, person2);

        final WorkingTimeCalendar personWorkingTimeCalendar = actual.get(person);
        for (LocalDate date : dateRange) {
            assertThat(personWorkingTimeCalendar.workingTime(date)).hasValue(BigDecimal.ONE);
        }

        final WorkingTimeCalendar person2WorkingTimeCalendar = actual.get(person2);
        for (LocalDate date : dateRange) {
            assertThat(person2WorkingTimeCalendar.workingTime(date)).hasValue(BigDecimal.valueOf(0.5));
        }
    }

    @Test
    void ensureGetWorkingTimesByPersonsAndYearUsesDefaultFederalStateWhenWorkingTimeDoesNotDefineIt() {
        final Person person = new Person();
        person.setId(1L);

        final List<Person> persons = List.of(person);

        final WorkingTimeEntity workingTimeEntity = createWorkingTimeEntity(person, LocalDate.of(2022, JANUARY, 1),
            FULL, FULL, FULL, FULL, FULL, FULL, FULL, null);

        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(persons)).thenReturn(List.of(workingTimeEntity));

        final PublicHolidaysSettings publicHolidaysSettings = new PublicHolidaysSettings();
        publicHolidaysSettings.setFederalState(GERMANY_BERLIN);
        final Settings settings = new Settings();
        settings.setPublicHolidaysSettings(publicHolidaysSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        sut.getWorkingTimesByPersons(persons, Year.of(2022));

        verify(publicHolidaysService).getPublicHolidays(eq(LocalDate.of(2022, JANUARY, 1)), eq(LocalDate.of(2022, DECEMBER, 31)), eq(GERMANY_BERLIN), any());
    }

    @Test
    void ensureGetWorkingTimesByPersonsAndYearIgnoresPublicHolidays() {
        final Person person = new Person();
        person.setId(1L);

        final Person person2 = new Person();
        person2.setId(2L);

        final List<Person> persons = List.of(person, person2);

        final WorkingTimeEntity workingTimeEntity = createWorkingTimeEntity(person, LocalDate.of(2022, JANUARY, 1),
            FULL, FULL, FULL, FULL, FULL, FULL, FULL, GERMANY_BADEN_WUERTTEMBERG);

        final WorkingTimeEntity workingTimeEntity2 = createWorkingTimeEntity(person2, LocalDate.of(2022, JANUARY, 1),
            DayLength.NOON, DayLength.NOON, DayLength.NOON, DayLength.NOON, DayLength.NOON, DayLength.NOON, DayLength.NOON, GERMANY_BERLIN);

        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(persons)).thenReturn(List.of(workingTimeEntity, workingTimeEntity2));

        when(publicHolidaysService.getPublicHolidays(any(LocalDate.class), any(LocalDate.class), eq(GERMANY_BADEN_WUERTTEMBERG), any()))
            .thenReturn(List.of(new PublicHoliday(LocalDate.of(2022, AUGUST, 5), FULL, "")));
        when(publicHolidaysService.getPublicHolidays(any(LocalDate.class), any(LocalDate.class), eq(GERMANY_BERLIN), any()))
            .thenReturn(List.of(new PublicHoliday(LocalDate.of(2022, AUGUST, 10), FULL, "")));

        final Map<Person, WorkingTimeCalendar> actual = sut.getWorkingTimesByPersons(persons, Year.of(2022));
        assertThat(actual)
            .hasSize(2)
            .containsKeys(person, person2);

        assertThat(actual.get(person).workingTime(LocalDate.of(2022, AUGUST, 5))).hasValue(BigDecimal.ZERO);
        assertThat(actual.get(person).workingTime(LocalDate.of(2022, AUGUST, 10))).hasValue(BigDecimal.ONE);
        assertThat(actual.get(person2).workingTime(LocalDate.of(2022, AUGUST, 5))).hasValue(BigDecimal.valueOf(0.5));
        assertThat(actual.get(person2).workingTime(LocalDate.of(2022, AUGUST, 10))).hasValue(BigDecimal.ZERO);
    }

    @Test
    void ensureGetWorkingTimesByPersonsAndYearIgnoresWorkingTimesNotInDateRange() {
        final Person person = new Person();
        person.setId(1L);

        final List<Person> persons = List.of(person);
        final DateRange dateRange = new DateRange(LocalDate.of(2022, JANUARY, 1), LocalDate.of(2022, DECEMBER, 31));

        final WorkingTimeEntity workingTimeEntity = createWorkingTimeEntity(person, LocalDate.of(2022, JANUARY, 1),
            FULL, FULL, FULL, FULL, FULL, FULL, FULL, GERMANY_BADEN_WUERTTEMBERG);

        final WorkingTimeEntity workingTimeNextYear = createWorkingTimeEntity(person, LocalDate.of(2023, JANUARY, 1),
            DayLength.NOON, DayLength.NOON, DayLength.NOON, DayLength.NOON, DayLength.NOON, DayLength.NOON, DayLength.NOON, GERMANY_BADEN_WUERTTEMBERG);

        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(persons))
            .thenReturn(List.of(workingTimeEntity, workingTimeNextYear));

        final Map<Person, WorkingTimeCalendar> actual = sut.getWorkingTimesByPersons(persons, Year.of(2022));
        assertThat(actual)
            .hasSize(1)
            .containsKey(person);

        final WorkingTimeCalendar workingTimeCalendar = actual.get(person);
        for (LocalDate date : dateRange) {
            assertThat(workingTimeCalendar.workingTime(date)).hasValue(BigDecimal.ONE);
        }

        assertThat(workingTimeCalendar.workingTime(LocalDate.of(2021, DECEMBER, 31))).isEmpty();
        assertThat(workingTimeCalendar.workingTime(LocalDate.of(2023, JANUARY, 1))).isEmpty();
    }

    @Test
    void ensureGetWorkingTimesByPersonsAndYearWithMultipleWorkingTimesInOneYear() {

        final Person person = new Person();
        person.setId(1L);

        final WorkingTimeEntity workingTimeEntity = createWorkingTimeEntity(person, LocalDate.of(2022, JANUARY, 1),
            FULL, FULL, FULL, FULL, FULL, FULL, FULL, SWITZERLAND_GENF);

        final WorkingTimeEntity workingTimeLaterInYear = createWorkingTimeEntity(person, LocalDate.of(2022, APRIL, 1),
            MORNING, MORNING, MORNING, MORNING, MORNING, MORNING, MORNING, GERMANY_BADEN_WUERTTEMBERG);

        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(List.of(person)))
            .thenReturn(List.of(workingTimeLaterInYear, workingTimeEntity));

        final Map<Person, WorkingTimeCalendar> actual = sut.getWorkingTimesByPersons(List.of(person), Year.of(2022));
        assertThat(actual)
            .hasSize(1)
            .containsKey(person);

        final WorkingTimeCalendar workingTimeCalendar = actual.get(person);
        for (LocalDate date : new DateRange(LocalDate.of(2022, JANUARY, 1), LocalDate.of(2022, MARCH, 31))) {
            assertThat(workingTimeCalendar.workingTime(date)).hasValue(BigDecimal.ONE);
        }
        for (LocalDate date : new DateRange(LocalDate.of(2022, APRIL, 1), LocalDate.of(2022, DECEMBER, 31))) {
            assertThat(workingTimeCalendar.workingTime(date)).hasValue(BigDecimal.valueOf(0.5));
        }
    }

    @Test
    void ensureGetWorkingTimesByPersonsAndYearReturnsEmptyWorkingTimeCalendarForPersonWithoutWorkingTime() {
        final Person person = new Person();
        person.setId(1L);

        final List<Person> persons = List.of(person);
        final DateRange dateRange = new DateRange(LocalDate.of(2022, JANUARY, 1), LocalDate.of(2022, DECEMBER, 31));

        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(persons)).thenReturn(List.of());

        final Map<Person, WorkingTimeCalendar> actual = sut.getWorkingTimesByPersons(persons, Year.of(2022));
        assertThat(actual)
            .hasSize(1)
            .containsKey(person);

        final WorkingTimeCalendar workingTimeCalendar = actual.get(person);
        for (LocalDate date : dateRange) {
            assertThat(workingTimeCalendar.workingTime(date)).isEmpty();
        }
    }

    @Test
    void ensureToHandlePublicHolidaysWithZeroDayLengthToFullWorkday() {
        final Person person = new Person();
        person.setId(1L);

        final List<Person> persons = List.of(person);

        final WorkingTimeEntity workingTimeEntity = createWorkingTimeEntity(person, LocalDate.of(2024, JANUARY, 1),
            FULL, FULL, FULL, FULL, FULL, FULL, FULL, GERMANY_BADEN_WUERTTEMBERG);
        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(persons)).thenReturn(List.of(workingTimeEntity));

        when(publicHolidaysService.getPublicHolidays(any(LocalDate.class), any(LocalDate.class), any(FederalState.class), any()))
            .thenReturn(List.of(new PublicHoliday(LocalDate.of(2024, DECEMBER, 24), ZERO, "")));

        final Map<Person, WorkingTimeCalendar> actual = sut.getWorkingTimesByPersons(persons, new DateRange(LocalDate.of(2024, DECEMBER, 24), LocalDate.of(2024, DECEMBER, 24)));
        assertThat(actual)
            .hasSize(1)
            .containsKeys(person);

        assertThat(actual.get(person).workingTime(LocalDate.of(2024, DECEMBER, 24))).hasValue(BigDecimal.ONE);
    }

    @Test
    void ensureGetWorkingTimesByPersonsFetchesThePublicHolidaysOncePerFederalState() {
        final Person person = new Person();
        person.setId(1L);
        final Person person2 = new Person();
        person2.setId(2L);
        final Person person3 = new Person();
        person3.setId(3L);

        final List<Person> persons = List.of(person, person2, person3);

        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(persons)).thenReturn(List.of(
            createWorkingTimeEntity(person, LocalDate.of(2022, JANUARY, 1), FULL, FULL, FULL, FULL, FULL, FULL, FULL, GERMANY_BADEN_WUERTTEMBERG),
            createWorkingTimeEntity(person2, LocalDate.of(2022, JANUARY, 1), FULL, FULL, FULL, FULL, FULL, FULL, FULL, GERMANY_BADEN_WUERTTEMBERG),
            createWorkingTimeEntity(person3, LocalDate.of(2022, JANUARY, 1), FULL, FULL, FULL, FULL, FULL, FULL, FULL, GERMANY_BERLIN)
        ));

        final LocalDate start = LocalDate.of(2022, JANUARY, 1);
        final LocalDate end = LocalDate.of(2022, DECEMBER, 31);

        when(publicHolidaysService.getPublicHolidays(eq(start), eq(end), eq(GERMANY_BADEN_WUERTTEMBERG), any()))
            .thenReturn(List.of(new PublicHoliday(LocalDate.of(2022, JANUARY, 6), FULL, "")));
        when(publicHolidaysService.getPublicHolidays(eq(start), eq(end), eq(GERMANY_BERLIN), any()))
            .thenReturn(List.of(new PublicHoliday(LocalDate.of(2022, MARCH, 8), FULL, "")));

        final Map<Person, WorkingTimeCalendar> actual = sut.getWorkingTimesByPersons(persons, Year.of(2022));

        assertThat(actual.get(person).workingTime(LocalDate.of(2022, JANUARY, 6))).hasValue(BigDecimal.ZERO);
        assertThat(actual.get(person2).workingTime(LocalDate.of(2022, JANUARY, 6))).hasValue(BigDecimal.ZERO);
        assertThat(actual.get(person3).workingTime(LocalDate.of(2022, JANUARY, 6))).hasValue(BigDecimal.ONE);
        assertThat(actual.get(person3).workingTime(LocalDate.of(2022, MARCH, 8))).hasValue(BigDecimal.ZERO);
        assertThat(actual.get(person).workingTime(LocalDate.of(2022, MARCH, 8))).hasValue(BigDecimal.ONE);

        // once per federal state, no matter how many persons share it - and never day by day
        verify(publicHolidaysService).getPublicHolidays(eq(start), eq(end), eq(GERMANY_BADEN_WUERTTEMBERG), any());
        verify(publicHolidaysService).getPublicHolidays(eq(start), eq(end), eq(GERMANY_BERLIN), any());
        verifyNoMoreInteractions(publicHolidaysService);
    }

    @Test
    void ensureGetWorkingTimesByPersonsUsesThePublicHolidaysOfTheFederalStateOfEachWorkingTime() {
        final Person person = new Person();
        person.setId(1L);

        final WorkingTimeEntity workingTimeGenf = createWorkingTimeEntity(person, LocalDate.of(2022, JANUARY, 1),
            FULL, FULL, FULL, FULL, FULL, FULL, FULL, SWITZERLAND_GENF);
        final WorkingTimeEntity workingTimeBadenWuerttemberg = createWorkingTimeEntity(person, LocalDate.of(2022, APRIL, 1),
            MORNING, MORNING, MORNING, MORNING, MORNING, MORNING, MORNING, GERMANY_BADEN_WUERTTEMBERG);

        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(List.of(person)))
            .thenReturn(List.of(workingTimeBadenWuerttemberg, workingTimeGenf));

        when(publicHolidaysService.getPublicHolidays(any(LocalDate.class), any(LocalDate.class), eq(SWITZERLAND_GENF), any()))
            .thenReturn(List.of(
                new PublicHoliday(LocalDate.of(2022, JANUARY, 3), FULL, ""),
                new PublicHoliday(LocalDate.of(2022, AUGUST, 1), FULL, "")));
        when(publicHolidaysService.getPublicHolidays(any(LocalDate.class), any(LocalDate.class), eq(GERMANY_BADEN_WUERTTEMBERG), any()))
            .thenReturn(List.of(
                new PublicHoliday(LocalDate.of(2022, JANUARY, 6), FULL, ""),
                new PublicHoliday(LocalDate.of(2022, OCTOBER, 3), FULL, "")));

        final WorkingTimeCalendar actual = sut.getWorkingTimesByPersons(List.of(person), Year.of(2022)).get(person);

        // Genf segment: Genf holiday applies, Baden-Württemberg holiday does not
        assertThat(actual.workingTime(LocalDate.of(2022, JANUARY, 3))).hasValue(BigDecimal.ZERO);
        assertThat(actual.workingTime(LocalDate.of(2022, JANUARY, 6))).hasValue(BigDecimal.ONE);
        // Baden-Württemberg segment (morning only): Baden-Württemberg holiday applies, Genf holiday does not
        assertThat(actual.workingTime(LocalDate.of(2022, OCTOBER, 3))).hasValue(BigDecimal.ZERO);
        assertThat(actual.workingTime(LocalDate.of(2022, AUGUST, 1))).hasValue(BigDecimal.valueOf(0.5));
    }

    @Test
    void ensureGetWorkingTimesByPersonsHandlesTwoPublicHolidaysOnTheSameDate() {
        final Person person = new Person();
        person.setId(1L);

        final List<Person> persons = List.of(person);

        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(persons)).thenReturn(List.of(
            createWorkingTimeEntity(person, LocalDate.of(2024, JANUARY, 1), FULL, FULL, FULL, FULL, FULL, FULL, FULL, GERMANY_BADEN_WUERTTEMBERG)));

        // a fixed and a movable public holiday can fall on the same date - both carry the day length of that date
        when(publicHolidaysService.getPublicHolidays(any(LocalDate.class), any(LocalDate.class), eq(GERMANY_BADEN_WUERTTEMBERG), any()))
            .thenReturn(List.of(
                new PublicHoliday(LocalDate.of(2024, DECEMBER, 24), MORNING, "first"),
                new PublicHoliday(LocalDate.of(2024, DECEMBER, 24), MORNING, "second")));

        final Map<Person, WorkingTimeCalendar> actual = sut.getWorkingTimesByPersons(persons, Year.of(2024));

        assertThat(actual.get(person).workingTime(LocalDate.of(2024, DECEMBER, 24))).hasValue(BigDecimal.valueOf(0.5));
    }

    // Helper method to create WorkingTimeEntity instances with consistent configuration
    private static WorkingTimeEntity createWorkingTimeEntity(Person person, LocalDate validFrom,
                                                             DayLength monday, DayLength tuesday, DayLength wednesday, DayLength thursday,
                                                             DayLength friday, DayLength saturday, DayLength sunday, FederalState federalState) {

        final WorkingTimeEntity entity = new WorkingTimeEntity();
        entity.setPerson(person);
        entity.setValidFrom(validFrom);
        entity.setMonday(monday);
        entity.setTuesday(tuesday);
        entity.setWednesday(wednesday);
        entity.setThursday(thursday);
        entity.setFriday(friday);
        entity.setSaturday(saturday);
        entity.setSunday(sunday);
        entity.setFederalStateOverride(federalState);
        return entity;
    }
}
