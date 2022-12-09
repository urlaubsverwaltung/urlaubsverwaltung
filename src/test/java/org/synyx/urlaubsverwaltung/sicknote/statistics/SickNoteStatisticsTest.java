package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static java.time.LocalDate.of;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.OCTOBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createSickNote;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

/**
 * Unit test for {@link SickNoteStatistics}.
 */
@ExtendWith(MockitoExtension.class)
class SickNoteStatisticsTest {

    @Mock
    private WorkDaysCountService workDaysCountService;

    @Test
    void testGetTotalNumberOfSickNotes() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate sickNote1from = of(2022, OCTOBER, 7);
        final LocalDate sickNote1To = of(2022, OCTOBER, 11);
        final SickNote sickNote1 = createSickNote(person, sickNote1from, sickNote1To, FULL);
        when(workDaysCountService.getWorkDaysCount(FULL, sickNote1from, sickNote1To, person)).thenReturn(new BigDecimal("5"));

        final LocalDate sickNote2From = of(2022, DECEMBER, 18);
        final SickNote sickNote2 = createSickNote(person, sickNote2From, of(2023, JANUARY, 3), FULL);
        when(workDaysCountService.getWorkDaysCount(FULL, sickNote2From, of(2022, DECEMBER, 31), person)).thenReturn(new BigDecimal("9"));

        final Clock fixedClock = Clock.fixed(Instant.parse("2022-10-17T00:00:00.00Z"), ZoneId.systemDefault());
        final SickNoteStatistics sut = new SickNoteStatistics(fixedClock, List.of(sickNote1, sickNote2), workDaysCountService);

        assertThat(sut.getTotalNumberOfSickNotes()).isEqualTo(2);
    }

    @Test
    void testGetTotalNumberOfSickDays() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate sickNote1from = of(2022, OCTOBER, 7);
        final LocalDate sickNote1To = of(2022, OCTOBER, 11);
        final SickNote sickNote1 = createSickNote(person, sickNote1from, sickNote1To, FULL);
        when(workDaysCountService.getWorkDaysCount(FULL, sickNote1from, sickNote1To, person)).thenReturn(new BigDecimal("5"));

        final LocalDate sickNote2From = of(2022, DECEMBER, 18);
        final SickNote sickNote2 = createSickNote(person, sickNote2From, of(2023, JANUARY, 3), FULL);
        when(workDaysCountService.getWorkDaysCount(FULL, sickNote2From, of(2022, DECEMBER, 31), person)).thenReturn(new BigDecimal("9"));

        final Clock fixedClock = Clock.fixed(Instant.parse("2022-10-17T00:00:00.00Z"), ZoneId.systemDefault());
        final SickNoteStatistics sut = new SickNoteStatistics(fixedClock, List.of(sickNote1, sickNote2), workDaysCountService);

        assertThat(sut.getTotalNumberOfSickDays()).isEqualTo(new BigDecimal("14"));
    }

    @Test
    void testGetAverageDurationOfDiseasePerPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate sickNote1from = of(2022, OCTOBER, 7);
        final LocalDate sickNote1To = of(2022, OCTOBER, 11);
        final SickNote sickNote1 = createSickNote(person, sickNote1from, sickNote1To, FULL);
        when(workDaysCountService.getWorkDaysCount(FULL, sickNote1from, sickNote1To, person)).thenReturn(new BigDecimal("5"));

        final Person person2 = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate sickNote2From = of(2022, DECEMBER, 18);
        final SickNote sickNote2 = createSickNote(person2, sickNote2From, of(2023, JANUARY, 3), FULL);
        when(workDaysCountService.getWorkDaysCount(FULL, sickNote2From, of(2022, DECEMBER, 31), person2)).thenReturn(new BigDecimal("9"));

        final Clock fixedClock = Clock.fixed(Instant.parse("2022-10-17T00:00:00.00Z"), ZoneId.systemDefault());
        final SickNoteStatistics sut = new SickNoteStatistics(fixedClock, List.of(sickNote1, sickNote2), workDaysCountService);

        // 2 sick notes: 1st with 5 workdays and 2nd with 9 workdays --> sum = 14 workdays
        // 14 workdays / 2 persons = 7 workdays per person
        final BigDecimal averageDurationOfDiseasePerPerson = sut.getAverageDurationOfDiseasePerPerson();
        assertThat(averageDurationOfDiseasePerPerson).isEqualByComparingTo(BigDecimal.valueOf(7));
    }

    @Test
    void testGetAverageDurationOfDiseasePerPersonDivisionByZero() {
        final Clock fixedClock = Clock.fixed(Instant.parse("2022-10-17T00:00:00.00Z"), ZoneId.systemDefault());

        final SickNoteStatistics sut = new SickNoteStatistics(fixedClock, List.of(), workDaysCountService);
        final BigDecimal averageDurationOfDiseasePerPerson = sut.getAverageDurationOfDiseasePerPerson();
        assertThat(averageDurationOfDiseasePerPerson).isEqualByComparingTo(ZERO);
    }

    @Test
    void ensuresThatAnYearOverSpanningSickNoteCalculatesOnlyTheWorkdaysOfTheRequestedYear() {
        final Clock fixedClock = Clock.fixed(Instant.parse("2015-10-17T00:00:00.00Z"), ZoneId.systemDefault());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate from = of(2014, DECEMBER, 7);
        final LocalDate to = of(2016, JANUARY, 11);
        final SickNote sickNote = createSickNote(person, from, to, FULL);
        final BigDecimal sickDays = new BigDecimal("9");
        when(workDaysCountService.getWorkDaysCount(FULL, of(2015, JANUARY, 1), of(2015, DECEMBER, 31), person)).thenReturn(sickDays);

        final SickNoteStatistics sut = new SickNoteStatistics(fixedClock, List.of(sickNote), workDaysCountService);
        assertThat(sut.getAverageDurationOfDiseasePerPerson()).isEqualByComparingTo(sickDays);
    }
}
