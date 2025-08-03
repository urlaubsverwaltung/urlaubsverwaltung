package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static java.time.LocalDate.of;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.OCTOBER;
import static java.util.Arrays.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarFactory.workingTimeCalendarMondayToFriday;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarFactory.workingTimeCalendarMondayToSunday;

@ExtendWith(MockitoExtension.class)
class SickNoteStatisticsTest {

    @Test
    void testGetTotalNumberOfSickNotes() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final SickNote sickNote1 = SickNote.builder()
            .person(person)
            .startDate(of(2022, OCTOBER, 7))
            .endDate(of(2022, OCTOBER, 11))
            .dayLength(FULL)
            .sickNoteType(sickNoteType())
            .status(SickNoteStatus.ACTIVE)
            .build();

        final SickNote sickNote2 = SickNote.builder()
            .person(person)
            .startDate(of(2022, DECEMBER, 18))
            .endDate(of(2023, JANUARY, 3))
            .dayLength(FULL)
            .sickNoteType(sickNoteType())
            .status(SickNoteStatus.ACTIVE)
            .build();

        final Year year = Year.of(2022);
        final LocalDate asOfDate = LocalDate.of(2022, 10, 17);
        final SickNoteStatistics sut = new SickNoteStatistics(year, asOfDate, List.of(sickNote1, sickNote2), List.of());

        assertThat(sut.getTotalNumberOfSickNotes()).isEqualTo(new BigDecimal(2));
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
            .status(SickNoteStatus.ACTIVE)
            .workingTimeCalendar(workingTimeCalendar)
            .build();

        final SickNote sickNoteChild = SickNote.builder()
            .person(person)
            .startDate(of(2022, DECEMBER, 18))
            .endDate(of(2023, JANUARY, 3))
            .dayLength(FULL)
            .sickNoteType(childSickNoteType())
            .status(SickNoteStatus.ACTIVE)
            .workingTimeCalendar(workingTimeCalendar)
            .build();

        final Year year = Year.of(2022);
        final LocalDate asOfDate = LocalDate.of(2022, 10, 17);
        final SickNoteStatistics sut = new SickNoteStatistics(year, asOfDate, List.of(sickNote, sickNoteChild), List.of());

        assertThat(sut.getTotalNumberOfSickDaysAllCategories()).isEqualTo(new BigDecimal(13));
    }

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
            .status(SickNoteStatus.ACTIVE)
            .workingTimeCalendar(personWorkingTimeCalendar)
            .build();

        final SickNote sickNote2 = SickNote.builder()
            .person(person2)
            .startDate(of(2022, DECEMBER, 18))
            .endDate(of(2023, JANUARY, 3))
            .dayLength(FULL)
            .sickNoteType(sickNoteType())
            .status(SickNoteStatus.ACTIVE)
            .workingTimeCalendar(person2WorkingTimeCalendar)
            .build();

        final Year year = Year.of(2022);
        final LocalDate asOfDate = LocalDate.of(2022, 10, 17);
        final SickNoteStatistics sut = new SickNoteStatistics(year, asOfDate, List.of(sickNote1, sickNote2), List.of());

        // 2 sick notes: 1st with 3 workdays and 2nd with 10 workdays in year of statistic --> sum = 13 workdays
        // 15 workdays / 2 persons = 6.5 workdays per person
        final BigDecimal averageDurationOfDiseasePerPerson = sut.getAverageDurationOfDiseasePerPerson();
        assertThat(averageDurationOfDiseasePerPerson).isEqualByComparingTo(BigDecimal.valueOf(6.5));
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
            .status(SickNoteStatus.ACTIVE)
            .workingTimeCalendar(workingTimeCalendar)
            .build();

        final Year year = Year.of(2015);
        final LocalDate asOfDate = LocalDate.of(2015, 10, 17);
        final SickNoteStatistics sut = new SickNoteStatistics(year, asOfDate, List.of(sickNote), List.of());

        // 2015 has 261 monday to friday workdays
        assertThat(sut.getAverageDurationOfDiseasePerPerson()).isEqualByComparingTo(new BigDecimal(261));
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
                .status(SickNoteStatus.ACTIVE)
                .workingTimeCalendar(workingTimeCalendar)
                .build();

            final LocalDate date2 = endDate.plusDays(1);

            final SickNote sickNote2 = SickNote.builder()
                .person(person)
                .startDate(date2)
                .endDate(date2)
                .dayLength(FULL)
                .sickNoteType(childSickNoteType())
                .status(SickNoteStatus.ACTIVE)
                .workingTimeCalendar(workingTimeCalendar)
                .build();

            final SickNote childSickNote = SickNote.builder(sickNote)
                .sickNoteType(childSickNoteType())
                .build();

//            when(workDaysCountService.getWorkDaysCount(FULL, startDate, endDate, person)).thenReturn(BigDecimal.valueOf(2));
//            when(workDaysCountService.getWorkDaysCount(FULL, date2, date2, person)).thenReturn(BigDecimal.ONE);

            final Year year = Year.of(2025);
            final LocalDate asOfDate = LocalDate.of(2022, 7, 4);
            final SickNoteStatistics sut = new SickNoteStatistics(year, asOfDate, List.of(sickNote, sickNote2, childSickNote), List.of());
            assertThat(sut.getNumberOfSickDaysByMonth()).isEqualTo(stream(Month.values()).map(month -> month.equals(givenMonth) ? BigDecimal.valueOf(2) : ZERO).toList());
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
                .status(SickNoteStatus.ACTIVE)
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
                .status(SickNoteStatus.ACTIVE)
                .workingTimeCalendar(workingTimeCalendar)
                .build();

            final Year year = Year.of(2025);
            final LocalDate asOfDate = LocalDate.of(2025, 7, 4);
            final SickNoteStatistics sut = new SickNoteStatistics(year, asOfDate, List.of(sickNote), List.of());
            assertThat(sut.getNumberOfSickDaysByMonth()).isEqualTo(List.of(BigDecimal.valueOf(1), BigDecimal.valueOf(2), ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO));
        }
    }

    private Person anyPerson() {
       return new Person("muster", "Muster", "Marlene", "muster@example.org");
    }

    private static SickNoteType sickNoteType() {
        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SickNoteCategory.SICK_NOTE);
        return sickNoteType;
    }

    private static SickNoteType childSickNoteType() {
        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SickNoteCategory.SICK_NOTE_CHILD);
        return sickNoteType;
    }
}
