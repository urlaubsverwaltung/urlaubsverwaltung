package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.OCTOBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link SickNoteStatistics}.
 */
@ExtendWith(MockitoExtension.class)
class SickNoteStatisticsTest {

    private SickNoteStatistics sut;

    @Mock
    private WorkDaysCountService workDaysCountService;
    @Mock
    private SickNoteService sickNoteDAO;

    private List<SickNote> sickNotes;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {

        sickNotes = new ArrayList<>();

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        SickNote sickNote1 = TestDataCreator.createSickNote(person,
            LocalDate.of(Year.now(clock).getValue(), OCTOBER, 7),
            LocalDate.of(Year.now(clock).getValue(), OCTOBER, 11), DayLength.FULL);

        SickNote sickNote2 = TestDataCreator.createSickNote(person,
            LocalDate.of(Year.now(clock).getValue(), DECEMBER, 18),
            LocalDate.of(Year.now(clock).getValue() + 1, JANUARY, 3), DayLength.FULL);

        sickNotes.add(sickNote1);
        sickNotes.add(sickNote2);

        when(sickNoteDAO.getNumberOfPersonsWithMinimumOneSickNote(Year.now(clock).getValue())).thenReturn(7L);
        when(sickNoteDAO.getAllActiveByYear(Year.now(clock).getValue())).thenReturn(sickNotes);

        when(workDaysCountService.getWorkDaysCount(DayLength.FULL, LocalDate.of(Year.now(clock).getValue(), OCTOBER, 7),
            LocalDate.of(Year.now(clock).getValue(), OCTOBER, 11), person))
            .thenReturn(new BigDecimal("5"));

        when(workDaysCountService.getWorkDaysCount(DayLength.FULL, LocalDate.of(Year.now(clock).getValue(), DECEMBER, 18),
            LocalDate.of(Year.now(clock).getValue(), DECEMBER, 31), person))
            .thenReturn(new BigDecimal("9"));


        sut = new SickNoteStatistics(clock, sickNoteDAO, workDaysCountService);
    }

    @Test
    void testGetTotalNumberOfSickNotes() {
        Assert.assertEquals(2, sut.getTotalNumberOfSickNotes());
    }

    @Test
    void testGetTotalNumberOfSickDays() {
        Assert.assertEquals(new BigDecimal("14"), sut.getTotalNumberOfSickDays());
    }

    @Test
    void testGetAverageDurationOfDiseasePerPerson() {

        // 2 sick notes: 1st with 5 workdays and 2nd with 9 workdays --> sum = 14 workdays
        // 14 workdays / 7 persons = 2 workdays per person
        sut = new SickNoteStatistics(clock, sickNoteDAO, workDaysCountService);

        final BigDecimal averageDurationOfDiseasePerPerson = sut.getAverageDurationOfDiseasePerPerson();
        assertThat(averageDurationOfDiseasePerPerson).isEqualByComparingTo(BigDecimal.valueOf(2));
    }

    @Test
    void testGetAverageDurationOfDiseasePerPersonDivisionByZero() {

        when(sickNoteDAO.getNumberOfPersonsWithMinimumOneSickNote(Year.now(clock).getValue())).thenReturn(0L);

        sut = new SickNoteStatistics(clock, sickNoteDAO, workDaysCountService);

        final BigDecimal averageDurationOfDiseasePerPerson = sut.getAverageDurationOfDiseasePerPerson();
        assertThat(averageDurationOfDiseasePerPerson).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void testGetTotalNumberOfSickDaysInvalidDateRange() {

        when(sickNoteDAO.getAllActiveByYear(2015)).thenReturn(sickNotes);

        Clock fixedClock = Clock.fixed(ZonedDateTime.now(this.clock).withYear(2015).toInstant(), this.clock.getZone());

        assertThatIllegalArgumentException().isThrownBy(
            () -> new SickNoteStatistics(fixedClock, sickNoteDAO, workDaysCountService));
    }
}
