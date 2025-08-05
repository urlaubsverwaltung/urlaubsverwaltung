package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;
import static java.time.LocalDate.of;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.OCTOBER;
import static java.util.Arrays.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarFactory.workingTimeCalendarMondayToFriday;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarFactory.workingTimeCalendarMondayToSunday;

@ExtendWith(MockitoExtension.class)
class SickNoteStatisticsTest {

    @Nested
    class TotalNumberOfSickNotes {

        @Test
        void ensureThatTotalNumberOfSickNotesAreCorrect() {

            final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

            final SickNote sickNote1 = SickNote.builder()
                .person(person)
                .startDate(of(2022, OCTOBER, 7))
                .endDate(of(2022, OCTOBER, 11))
                .dayLength(FULL)
                .sickNoteType(sickNoteType())
                .status(ACTIVE)
                .build();

            final SickNote sickNote2 = SickNote.builder()
                .person(person)
                .startDate(of(2022, DECEMBER, 18))
                .endDate(of(2023, JANUARY, 3))
                .dayLength(FULL)
                .sickNoteType(childSickNoteType())
                .status(ACTIVE)
                .build();

            final Year year = Year.of(2022);
            final LocalDate asOfDate = LocalDate.of(2022, 10, 17);
            final SickNoteStatistics sut = new SickNoteStatistics(year, asOfDate, List.of(sickNote1, sickNote2), List.of());

            assertThat(sut.getTotalNumberOfAllSickNotes()).isEqualTo(2);
            assertThat(sut.getTotalNumberOfChildSickNotes()).isEqualTo(BigDecimal.ONE);
            assertThat(sut.getTotalNumberOfSickNotes()).isEqualTo(BigDecimal.ONE);
        }
    }

    @Test
    void testGetTotalNumberOfSickNotes() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final SickNote sickNote1 = SickNote.builder()
            .person(person)
            .startDate(of(2022, OCTOBER, 7))
            .endDate(of(2022, OCTOBER, 11))
            .dayLength(FULL)
            .sickNoteType(sickNoteType())
            .status(ACTIVE)
            .build();

        final SickNote sickNote2 = SickNote.builder()
            .person(person)
            .startDate(of(2022, DECEMBER, 18))
            .endDate(of(2023, JANUARY, 3))
            .dayLength(FULL)
            .sickNoteType(sickNoteType())
            .status(ACTIVE)
            .build();

        final Year year = Year.of(2022);
        final LocalDate asOfDate = LocalDate.of(2022, 10, 17);
        final SickNoteStatistics sut = new SickNoteStatistics(year, asOfDate, List.of(sickNote1, sickNote2), List.of());

        assertThat(sut.getTotalNumberOfSickNotes()).isEqualTo(new BigDecimal(2));
    }

    @Nested
    class AverageOfSickNotes {

        @Test
        void ensureAverageDurationOfAllSickNotes() {

            final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

            final LocalDate start = LocalDate.parse("2022-01-01");
            final LocalDate end = LocalDate.parse("2022-12-31");
            final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarMondayToFriday(start, end);

            final SickNote sickNote1 = SickNote.builder()
                .person(person)
                .startDate(of(2022, OCTOBER, 7))
                .endDate(of(2022, OCTOBER, 11))
                .dayLength(FULL)
                .sickNoteType(sickNoteType())
                .status(ACTIVE)
                .workingTimeCalendar(workingTimeCalendar)
                .build();

            final SickNote sickNote2 = SickNote.builder()
                .person(person)
                .startDate(of(2022, DECEMBER, 18))
                .endDate(of(2023, JANUARY, 3))
                .dayLength(FULL)
                .sickNoteType(childSickNoteType())
                .status(ACTIVE)
                .workingTimeCalendar(workingTimeCalendar)
                .build();

            final Year year = Year.of(2022);
            final LocalDate asOfDate = LocalDate.of(2022, 10, 17);
            final SickNoteStatistics sut = new SickNoteStatistics(year, asOfDate, List.of(sickNote1, sickNote2), List.of(person));

            assertThat(sut.getAverageDurationOfAllSickNotes()).isEqualTo(valueOf(6.50).setScale(2, HALF_UP));
            assertThat(sut.getAverageDurationOfSickNote()).isEqualTo(valueOf(3.00).setScale(2, HALF_UP));
            assertThat(sut.getAverageDurationOfChildSickNote()).isEqualTo(valueOf(10.00).setScale(2, HALF_UP));
        }
    }

    @Test
    void testGetTotalNumberOfSickDaysAllCategories() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate start = LocalDate.parse("2022-01-01");
        final LocalDate end = LocalDate.parse("2022-12-31");
        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarMondayToFriday(start, end);

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .startDate(of(2022, OCTOBER, 7))
            .endDate(of(2022, OCTOBER, 11))
            .dayLength(FULL)
            .sickNoteType(sickNoteType())
            .status(ACTIVE)
            .workingTimeCalendar(workingTimeCalendar)
            .build();

        final SickNote sickNoteChild = SickNote.builder()
            .person(person)
            .startDate(of(2022, DECEMBER, 18))
            .endDate(of(2023, JANUARY, 3))
            .dayLength(FULL)
            .sickNoteType(childSickNoteType())
            .status(ACTIVE)
            .workingTimeCalendar(workingTimeCalendar)
            .build();

        final Year year = Year.of(2022);
        final LocalDate asOfDate = LocalDate.of(2022, 10, 17);
        final SickNoteStatistics sut = new SickNoteStatistics(year, asOfDate, List.of(sickNote, sickNoteChild), List.of());

        assertThat(sut.getTotalNumberOfSickDaysAllCategories()).isEqualTo(new BigDecimal(13));
    }

    @Nested
    class AverageDurationOfDiseasePerPerson {

        @Test
        void testGetAverageDurationOfDiseasePerPerson() {

            final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
            final Person person2 = new Person("muster", "Muster", "Marlene", "muster@example.org");

            final LocalDate start = LocalDate.parse("2022-01-01");
            final LocalDate end = LocalDate.parse("2022-12-31");
            final WorkingTimeCalendar personWorkingTimeCalendar = workingTimeCalendarMondayToFriday(start, end);
            final WorkingTimeCalendar person2WorkingTimeCalendar = workingTimeCalendarMondayToFriday(start, end);

            final SickNote sickNote1 = SickNote.builder()
                .person(person)
                .startDate(of(2022, OCTOBER, 7))
                .endDate(of(2022, OCTOBER, 11))
                .dayLength(FULL)
                .sickNoteType(sickNoteType())
                .status(ACTIVE)
                .workingTimeCalendar(personWorkingTimeCalendar)
                .build();

            final SickNote sickNote2 = SickNote.builder()
                .person(person2)
                .startDate(of(2022, DECEMBER, 18))
                .endDate(of(2023, JANUARY, 3))
                .dayLength(FULL)
                .sickNoteType(sickNoteType())
                .status(ACTIVE)
                .workingTimeCalendar(person2WorkingTimeCalendar)
                .build();

            final Year year = Year.of(2022);
            final LocalDate asOfDate = LocalDate.of(2022, 10, 17);
            final SickNoteStatistics sut = new SickNoteStatistics(year, asOfDate, List.of(sickNote1, sickNote2), List.of());

            // 2 sick notes: 1st with 3 workdays and 2nd with 10 workdays in year of statistic --> sum = 13 workdays
            // 15 workdays / 2 persons = 6.5 workdays per person
            final BigDecimal averageDurationOfDiseasePerPerson = sut.getAverageDurationOfDiseasePerPerson();
            assertThat(averageDurationOfDiseasePerPerson).isEqualByComparingTo(valueOf(6.5));
        }

        @Test
        void testGetAverageDurationOfDiseasePerPersonDivisionByZero() {
            final Year year = Year.of(2022);
            final LocalDate asOfDate = LocalDate.of(2022, 10, 17);
            final SickNoteStatistics sut = new SickNoteStatistics(year, asOfDate, List.of(), List.of());

            final BigDecimal averageDurationOfDiseasePerPerson = sut.getAverageDurationOfDiseasePerPerson();
            assertThat(averageDurationOfDiseasePerPerson).isEqualByComparingTo(ZERO);
        }

        @Test
        void ensuresThatAnYearOverSpanningSickNoteCalculatesOnlyTheWorkdaysOfTheRequestedYear() {

            final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

            // building workingTimeCalendar for the year 2015 only on purpose
            // 2014 and 2016 should not be considered since statistics are only for 2015
            final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarMondayToFriday(LocalDate.parse("2015-01-01"), LocalDate.parse("2025-12-31"));

            final SickNote sickNote = SickNote.builder()
                .person(person)
                .startDate(of(2014, DECEMBER, 7))
                .endDate(of(2016, JANUARY, 11))
                .dayLength(FULL)
                .sickNoteType(sickNoteType())
                .status(ACTIVE)
                .workingTimeCalendar(workingTimeCalendar)
                .build();

            final Year year = Year.of(2015);
            final LocalDate asOfDate = LocalDate.of(2015, 10, 17);
            final SickNoteStatistics sut = new SickNoteStatistics(year, asOfDate, List.of(sickNote), List.of());

            // 2015 has 261 monday to friday workdays
            assertThat(sut.getAverageDurationOfDiseasePerPerson()).isEqualByComparingTo(new BigDecimal(261));
        }

        @Test
        void ensureAverageDurationOfDiseasePerPersonForAllTypes() {

            final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
            final Person person2 = new Person("muster", "Muster", "Marlene", "muster@example.org");

            final LocalDate start = LocalDate.parse("2022-01-01");
            final LocalDate end = LocalDate.parse("2022-12-31");
            final WorkingTimeCalendar personWorkingTimeCalendar = workingTimeCalendarMondayToFriday(start, end);
            final WorkingTimeCalendar person2WorkingTimeCalendar = workingTimeCalendarMondayToFriday(start, end);

            final SickNote sickNote1 = SickNote.builder()
                .person(person)
                .startDate(of(2022, OCTOBER, 7))
                .endDate(of(2022, OCTOBER, 11))
                .dayLength(FULL)
                .sickNoteType(sickNoteType())
                .status(ACTIVE)
                .workingTimeCalendar(personWorkingTimeCalendar)
                .build();

            final SickNote sickNote2 = SickNote.builder()
                .person(person2)
                .startDate(of(2022, DECEMBER, 18))
                .endDate(of(2023, JANUARY, 3))
                .dayLength(FULL)
                .sickNoteType(childSickNoteType())
                .status(ACTIVE)
                .workingTimeCalendar(person2WorkingTimeCalendar)
                .build();

            final Year year = Year.of(2022);
            final LocalDate asOfDate = LocalDate.of(2022, 10, 17);
            final SickNoteStatistics sut = new SickNoteStatistics(year, asOfDate, List.of(sickNote1, sickNote2), List.of());

            assertThat(sut.getAverageDurationOfDiseasePerPerson()).isEqualByComparingTo(valueOf(6.5));
            assertThat(sut.getAverageDurationOfDiseasePerPersonAndSick()).isEqualByComparingTo(valueOf(1.5));
            assertThat(sut.getAverageDurationOfDiseasePerPersonAndChildSick()).isEqualByComparingTo(valueOf(5.00));
        }
    }

    @Nested
    class NumberOfPersonsWithAtLeastOneSickNoteGraph {

        @Test
        void ensureNumberOfPersonsWithAtLeastOneSickNoteAndWithoutSickNotesAreCorrect() {

            final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
            final Person person2 = new Person("master", "Master", "Marlon", "master@example.org");
            final Person person3 = new Person("moster", "Moster", "Partlon", "moster@example.org");

            final LocalDate start = LocalDate.parse("2022-01-01");
            final LocalDate end = LocalDate.parse("2022-12-31");
            final WorkingTimeCalendar personWorkingTimeCalendar = workingTimeCalendarMondayToFriday(start, end);
            final WorkingTimeCalendar person2WorkingTimeCalendar = workingTimeCalendarMondayToFriday(start, end);

            final SickNote sickNote1 = SickNote.builder()
                .person(person)
                .startDate(of(2022, OCTOBER, 7))
                .endDate(of(2022, OCTOBER, 11))
                .dayLength(FULL)
                .sickNoteType(sickNoteType())
                .status(ACTIVE)
                .workingTimeCalendar(personWorkingTimeCalendar)
                .build();

            final SickNote sickNote2 = SickNote.builder()
                .person(person2)
                .startDate(of(2022, DECEMBER, 18))
                .endDate(of(2023, JANUARY, 3))
                .dayLength(FULL)
                .sickNoteType(sickNoteType())
                .status(ACTIVE)
                .workingTimeCalendar(person2WorkingTimeCalendar)
                .build();

            final Year year = Year.of(2022);
            final LocalDate asOfDate = LocalDate.of(2022, 10, 17);
            final SickNoteStatistics sut = new SickNoteStatistics(year, asOfDate, List.of(sickNote1, sickNote2), List.of(person, person2, person3));

            assertThat(sut.getNumberOfPersonsWithMinimumOneSickNote()).isEqualTo(2L);
            assertThat(sut.getNumberOfPersonsWithoutSickNote()).isEqualTo(1L);
            assertThat(sut.getAtLeastOneSickNotePercent()).isEqualTo(valueOf(66.700).setScale(3, HALF_UP));
        }
    }

    @Nested
    class TotalNumberOfSickDays {

        @Test
        void ensureTotalNumberOfSickDaysAreCorrectOverTimeSpan() {

            final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
            final Person person2 = new Person("master", "Master", "Marlon", "master@example.org");

            final LocalDate start = LocalDate.parse("2022-01-01");
            final LocalDate end = LocalDate.parse("2022-12-31");
            final WorkingTimeCalendar personWorkingTimeCalendar = workingTimeCalendarMondayToFriday(start, end);
            final WorkingTimeCalendar person2WorkingTimeCalendar = workingTimeCalendarMondayToFriday(start, end);

            final SickNote sickNote1 = SickNote.builder()
                .person(person)
                .startDate(of(2022, OCTOBER, 7))
                .endDate(of(2022, OCTOBER, 11))
                .dayLength(FULL)
                .sickNoteType(sickNoteType())
                .status(ACTIVE)
                .workingTimeCalendar(personWorkingTimeCalendar)
                .build();

            final SickNote sickNote2 = SickNote.builder()
                .person(person2)
                .startDate(of(2022, DECEMBER, 18))
                .endDate(of(2023, JANUARY, 3))
                .dayLength(FULL)
                .sickNoteType(childSickNoteType())
                .status(ACTIVE)
                .workingTimeCalendar(person2WorkingTimeCalendar)
                .build();

            final Year year = Year.of(2022);
            final LocalDate asOfDate = LocalDate.of(2022, 10, 17);
            final SickNoteStatistics sut = new SickNoteStatistics(year, asOfDate, List.of(sickNote1, sickNote2), List.of(person, person2));

            assertThat(sut.getTotalNumberOfSickDaysAllCategories()).isEqualTo(valueOf(13));
            assertThat(sut.getTotalNumberOfSickDays()).isEqualTo(valueOf(3));
            assertThat(sut.getTotalNumberOfChildSickDays()).isEqualTo(valueOf(10));
        }

    }

    @Nested
    class NumberOfSickDaysByMonth {

        @ParameterizedTest
        @EnumSource(Month.class)
        void ensureForMonth(Month givenMonth) {
            final Person person = anyPerson();

            final LocalDate startDate = of(2025, givenMonth.getValue(), 1);
            final LocalDate endDate = of(2025, givenMonth.getValue(), 2);

            final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarMondayToSunday(LocalDate.parse("2025-01-01"), LocalDate.parse("2025-12-31"));

            final SickNote sickNote = SickNote.builder()
                .person(person)
                .startDate(startDate)
                .endDate(endDate)
                .dayLength(FULL)
                .sickNoteType(sickNoteType())
                .status(ACTIVE)
                .workingTimeCalendar(workingTimeCalendar)
                .build();

            final LocalDate date2 = endDate.plusDays(1);

            final SickNote sickNote2 = SickNote.builder()
                .person(person)
                .startDate(date2)
                .endDate(date2)
                .dayLength(FULL)
                .sickNoteType(childSickNoteType())
                .status(ACTIVE)
                .workingTimeCalendar(workingTimeCalendar)
                .build();

            final SickNote childSickNote = SickNote.builder(sickNote)
                .sickNoteType(childSickNoteType())
                .build();

            final Year year = Year.of(2025);
            final LocalDate asOfDate = LocalDate.of(2022, 7, 4);
            final SickNoteStatistics sut = new SickNoteStatistics(year, asOfDate, List.of(sickNote, sickNote2, childSickNote), List.of());
            assertThat(sut.getNumberOfSickDaysByMonth()).isEqualTo(stream(Month.values()).map(month -> month.equals(givenMonth) ? valueOf(2) : ZERO).toList());
        }

        @Test
        void ensureIgnoresChildSickNotes() {
            final Person person = anyPerson();

            final SickNote childSickNote = SickNote.builder(SickNote.builder()
                .person(person)
                .startDate(of(2025, 1, 1))
                .endDate(of(2025, 1, 2))
                .dayLength(FULL)
                .sickNoteType(sickNoteType())
                .status(ACTIVE)
                .build()).sickNoteType(childSickNoteType()).build();

            final Year year = Year.of(2025);
            final LocalDate asOfDate = LocalDate.of(2025, 7, 4);

            final SickNoteStatistics sut = new SickNoteStatistics(year, asOfDate, List.of(childSickNote), List.of());
            assertThat(sut.getNumberOfSickDaysByMonth()).isEqualTo(List.of(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO));
        }

        @Test
        void ensureWithMonthOverlap() {
            final Person person = anyPerson();

            final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarMondayToSunday(
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2025-12-31")
            );

            final SickNote sickNote = SickNote.builder()
                .person(person)
                .startDate(LocalDate.parse("2025-01-31"))
                .endDate(LocalDate.parse("2025-02-02"))
                .dayLength(FULL)
                .sickNoteType(sickNoteType())
                .status(ACTIVE)
                .workingTimeCalendar(workingTimeCalendar)
                .build();

            final Year year = Year.of(2025);
            final LocalDate asOfDate = LocalDate.of(2025, 7, 4);
            final SickNoteStatistics sut = new SickNoteStatistics(year, asOfDate, List.of(sickNote), List.of());
            assertThat(sut.getNumberOfSickDaysByMonth()).isEqualTo(List.of(valueOf(1), valueOf(2), ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO));
        }
    }

    private Person anyPerson() {
        return new Person("muster", "Muster", "Marlene", "muster@example.org");
    }

    private static SickNoteType sickNoteType() {
        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SICK_NOTE);
        return sickNoteType;
    }

    private static SickNoteType childSickNoteType() {
        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SICK_NOTE_CHILD);
        return sickNoteType;
    }
}
