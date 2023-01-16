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
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.Month.APRIL;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.JUNE;
import static java.time.Month.MARCH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
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
    void ensureGetWorkingTimesByPersonsAndYear() {
        final Person person = new Person();
        person.setId(1);

        final Person person2 = new Person();
        person2.setId(2);

        final List<Person> persons = List.of(person, person2);
        final DateRange dateRange = new DateRange(LocalDate.of(2022, JANUARY, 1), LocalDate.of(2022, DECEMBER, 31));

        final WorkingTimeEntity workingTimeEntity = new WorkingTimeEntity();
        workingTimeEntity.setValidFrom(LocalDate.of(2022, JANUARY, 1));
        workingTimeEntity.setPerson(person);
        workingTimeEntity.setMonday(FULL);
        workingTimeEntity.setTuesday(FULL);
        workingTimeEntity.setWednesday(FULL);
        workingTimeEntity.setThursday(FULL);
        workingTimeEntity.setFriday(FULL);
        workingTimeEntity.setSaturday(FULL);
        workingTimeEntity.setSunday(FULL);
        workingTimeEntity.setFederalStateOverride(GERMANY_BADEN_WUERTTEMBERG);

        final WorkingTimeEntity workingTimeEntity2 = new WorkingTimeEntity();
        workingTimeEntity2.setValidFrom(LocalDate.of(2022, JANUARY, 1));
        workingTimeEntity2.setPerson(person2);
        workingTimeEntity2.setMonday(DayLength.NOON);
        workingTimeEntity2.setTuesday(DayLength.NOON);
        workingTimeEntity2.setWednesday(DayLength.NOON);
        workingTimeEntity2.setThursday(DayLength.NOON);
        workingTimeEntity2.setFriday(DayLength.NOON);
        workingTimeEntity2.setSaturday(DayLength.NOON);
        workingTimeEntity2.setSunday(DayLength.NOON);
        workingTimeEntity2.setFederalStateOverride(GERMANY_BADEN_WUERTTEMBERG);

        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(persons))
            .thenReturn(List.of(workingTimeEntity, workingTimeEntity2));

        when(settingsService.getSettings()).thenReturn(new Settings());

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
        person.setId(1);

        final Person person2 = new Person();
        person2.setId(2);

        final List<Person> persons = List.of(person, person2);
        final DateRange dateRange = new DateRange(LocalDate.of(2022, JUNE, 1), LocalDate.of(2022, JUNE, 30));

        final WorkingTimeEntity workingTimeEntity = new WorkingTimeEntity();
        workingTimeEntity.setValidFrom(LocalDate.of(2022, JUNE, 1));
        workingTimeEntity.setPerson(person);
        workingTimeEntity.setMonday(FULL);
        workingTimeEntity.setTuesday(FULL);
        workingTimeEntity.setWednesday(FULL);
        workingTimeEntity.setThursday(FULL);
        workingTimeEntity.setFriday(FULL);
        workingTimeEntity.setSaturday(FULL);
        workingTimeEntity.setSunday(FULL);
        workingTimeEntity.setFederalStateOverride(GERMANY_BADEN_WUERTTEMBERG);

        final WorkingTimeEntity workingTimeEntity2 = new WorkingTimeEntity();
        workingTimeEntity2.setValidFrom(LocalDate.of(2022, JUNE, 1));
        workingTimeEntity2.setPerson(person2);
        workingTimeEntity2.setMonday(DayLength.NOON);
        workingTimeEntity2.setTuesday(DayLength.NOON);
        workingTimeEntity2.setWednesday(DayLength.NOON);
        workingTimeEntity2.setThursday(DayLength.NOON);
        workingTimeEntity2.setFriday(DayLength.NOON);
        workingTimeEntity2.setSaturday(DayLength.NOON);
        workingTimeEntity2.setSunday(DayLength.NOON);
        workingTimeEntity2.setFederalStateOverride(GERMANY_BADEN_WUERTTEMBERG);

        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(persons))
                .thenReturn(List.of(workingTimeEntity, workingTimeEntity2));

        when(settingsService.getSettings()).thenReturn(new Settings());

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
        person.setId(1);

        final List<Person> persons = List.of(person);

        final WorkingTimeEntity workingTimeEntity = new WorkingTimeEntity();
        workingTimeEntity.setValidFrom(LocalDate.of(2022, JANUARY, 1));
        workingTimeEntity.setPerson(person);
        workingTimeEntity.setMonday(FULL);
        workingTimeEntity.setTuesday(FULL);
        workingTimeEntity.setWednesday(FULL);
        workingTimeEntity.setThursday(FULL);
        workingTimeEntity.setFriday(FULL);
        workingTimeEntity.setSaturday(FULL);
        workingTimeEntity.setSunday(FULL);
        workingTimeEntity.setFederalStateOverride(null);

        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(persons)).thenReturn(List.of(workingTimeEntity));

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setFederalState(GERMANY_BERLIN);
        final Settings settings = new Settings();
        settings.setWorkingTimeSettings(workingTimeSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        sut.getWorkingTimesByPersons(persons, Year.of(2022));

        verify(publicHolidaysService, times(365)).getPublicHoliday(any(LocalDate.class), eq(GERMANY_BERLIN), eq(workingTimeSettings));
    }

    @Test
    void ensureGetWorkingTimesByPersonsAndYearIgnoresPublicHolidays() {
        final Person person = new Person();
        person.setId(1);

        final Person person2 = new Person();
        person2.setId(2);

        final List<Person> persons = List.of(person, person2);

        final WorkingTimeEntity workingTimeEntity = new WorkingTimeEntity();
        workingTimeEntity.setValidFrom(LocalDate.of(2022, JANUARY, 1));
        workingTimeEntity.setPerson(person);
        workingTimeEntity.setMonday(FULL);
        workingTimeEntity.setTuesday(FULL);
        workingTimeEntity.setWednesday(FULL);
        workingTimeEntity.setThursday(FULL);
        workingTimeEntity.setFriday(FULL);
        workingTimeEntity.setSaturday(FULL);
        workingTimeEntity.setSunday(FULL);
        workingTimeEntity.setFederalStateOverride(GERMANY_BADEN_WUERTTEMBERG);

        final WorkingTimeEntity workingTimeEntity2 = new WorkingTimeEntity();
        workingTimeEntity2.setValidFrom(LocalDate.of(2022, JANUARY, 1));
        workingTimeEntity2.setPerson(person2);
        workingTimeEntity2.setMonday(DayLength.NOON);
        workingTimeEntity2.setTuesday(DayLength.NOON);
        workingTimeEntity2.setWednesday(DayLength.NOON);
        workingTimeEntity2.setThursday(DayLength.NOON);
        workingTimeEntity2.setFriday(DayLength.NOON);
        workingTimeEntity2.setSaturday(DayLength.NOON);
        workingTimeEntity2.setSunday(DayLength.NOON);
        workingTimeEntity2.setFederalStateOverride(GERMANY_BERLIN);

        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(persons)).thenReturn(List.of(workingTimeEntity, workingTimeEntity2));

        final Settings settings = new Settings();
        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        settings.setWorkingTimeSettings(workingTimeSettings);
        when(settingsService.getSettings()).thenReturn(settings);
        when(publicHolidaysService.getPublicHoliday(any(LocalDate.class), any(FederalState.class), eq(workingTimeSettings))).thenReturn(Optional.empty());
        when(publicHolidaysService.getPublicHoliday(LocalDate.of(2022, AUGUST, 5), GERMANY_BADEN_WUERTTEMBERG, workingTimeSettings))
            .thenReturn(Optional.of(new PublicHoliday(LocalDate.of(2022, AUGUST, 5), FULL, "")));
        when(publicHolidaysService.getPublicHoliday(LocalDate.of(2022, AUGUST, 10), GERMANY_BERLIN, workingTimeSettings))
            .thenReturn(Optional.of(new PublicHoliday(LocalDate.of(2022, AUGUST, 10), FULL, "")));

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
        person.setId(1);

        final List<Person> persons = List.of(person);
        final DateRange dateRange = new DateRange(LocalDate.of(2022, JANUARY, 1), LocalDate.of(2022, DECEMBER, 31));

        final WorkingTimeEntity workingTimeEntity = new WorkingTimeEntity();
        workingTimeEntity.setValidFrom(LocalDate.of(2022, JANUARY, 1));
        workingTimeEntity.setPerson(person);
        workingTimeEntity.setMonday(FULL);
        workingTimeEntity.setTuesday(FULL);
        workingTimeEntity.setWednesday(FULL);
        workingTimeEntity.setThursday(FULL);
        workingTimeEntity.setFriday(FULL);
        workingTimeEntity.setSaturday(FULL);
        workingTimeEntity.setSunday(FULL);
        workingTimeEntity.setFederalStateOverride(GERMANY_BADEN_WUERTTEMBERG);

        final WorkingTimeEntity workingTimePreviousYear = new WorkingTimeEntity();
        workingTimePreviousYear.setValidFrom(LocalDate.of(2021, JANUARY, 1));
        workingTimePreviousYear.setPerson(person);
        workingTimePreviousYear.setMonday(MORNING);
        workingTimePreviousYear.setTuesday(MORNING);
        workingTimePreviousYear.setWednesday(MORNING);
        workingTimePreviousYear.setThursday(MORNING);
        workingTimePreviousYear.setFriday(MORNING);
        workingTimePreviousYear.setSaturday(MORNING);
        workingTimePreviousYear.setSunday(MORNING);
        workingTimePreviousYear.setFederalStateOverride(GERMANY_BADEN_WUERTTEMBERG);

        final WorkingTimeEntity workingTimeNextYear = new WorkingTimeEntity();
        workingTimeNextYear.setValidFrom(LocalDate.of(2023, JANUARY, 1));
        workingTimeNextYear.setPerson(person);
        workingTimeNextYear.setMonday(DayLength.NOON);
        workingTimeNextYear.setTuesday(DayLength.NOON);
        workingTimeNextYear.setWednesday(DayLength.NOON);
        workingTimeNextYear.setThursday(DayLength.NOON);
        workingTimeNextYear.setFriday(DayLength.NOON);
        workingTimeNextYear.setSaturday(DayLength.NOON);
        workingTimeNextYear.setSunday(DayLength.NOON);
        workingTimeNextYear.setFederalStateOverride(GERMANY_BADEN_WUERTTEMBERG);

        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(persons))
            .thenReturn(List.of(workingTimeEntity, workingTimeNextYear));

        when(settingsService.getSettings()).thenReturn(new Settings());

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
        person.setId(1);

        final WorkingTimeEntity workingTimeEntity = new WorkingTimeEntity();
        workingTimeEntity.setValidFrom(LocalDate.of(2022, JANUARY, 1));
        workingTimeEntity.setPerson(person);
        workingTimeEntity.setMonday(FULL);
        workingTimeEntity.setTuesday(FULL);
        workingTimeEntity.setWednesday(FULL);
        workingTimeEntity.setThursday(FULL);
        workingTimeEntity.setFriday(FULL);
        workingTimeEntity.setSaturday(FULL);
        workingTimeEntity.setSunday(FULL);
        workingTimeEntity.setFederalStateOverride(SWITZERLAND_GENF);

        final WorkingTimeEntity workingTimeLaterInYear = new WorkingTimeEntity();
        workingTimeLaterInYear.setValidFrom(LocalDate.of(2022, APRIL, 1));
        workingTimeLaterInYear.setPerson(person);
        workingTimeLaterInYear.setMonday(MORNING);
        workingTimeLaterInYear.setTuesday(MORNING);
        workingTimeLaterInYear.setWednesday(MORNING);
        workingTimeLaterInYear.setThursday(MORNING);
        workingTimeLaterInYear.setFriday(MORNING);
        workingTimeLaterInYear.setSaturday(MORNING);
        workingTimeLaterInYear.setSunday(MORNING);
        workingTimeLaterInYear.setFederalStateOverride(GERMANY_BADEN_WUERTTEMBERG);

        when(workingTimeRepository.findByPersonIsInOrderByValidFromDesc(List.of(person)))
            .thenReturn(List.of(workingTimeLaterInYear, workingTimeEntity));

        when(settingsService.getSettings()).thenReturn(new Settings());

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
        person.setId(1);

        final List<Person> persons = List.of(person);
        final DateRange dateRange = new DateRange(LocalDate.of(2022, JANUARY, 1), LocalDate.of(2022, DECEMBER, 31));

        when(settingsService.getSettings()).thenReturn(new Settings());

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
}
