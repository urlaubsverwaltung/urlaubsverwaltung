package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.OCTOBER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link SickNoteStatistics}.
 */
public class SickNoteStatisticsTest {

    private SickNoteStatistics statistics;
    private WorkDaysService calendarService;
    private SickNoteService sickNoteDAO;
    private List<SickNote> sickNotes;
    private Clock clock = Clock.systemUTC();

    @Before
    public void setUp() {

        calendarService = mock(WorkDaysService.class);
        sickNoteDAO = mock(SickNoteService.class);
        sickNotes = new ArrayList<>();

        Person person = TestDataCreator.createPerson();

        SickNote sickNote1 = TestDataCreator.createSickNote(person,
            LocalDate.of(2013, OCTOBER, 7),
            LocalDate.of(2013, OCTOBER, 11), DayLength.FULL);

        SickNote sickNote2 = TestDataCreator.createSickNote(person,
            LocalDate.of(2013, DECEMBER, 18),
            LocalDate.of(2014, JANUARY, 3), DayLength.FULL);

        sickNotes.add(sickNote1);
        sickNotes.add(sickNote2);

        when(sickNoteDAO.getNumberOfPersonsWithMinimumOneSickNote(2013)).thenReturn(7L);
        when(sickNoteDAO.getAllActiveByYear(2013)).thenReturn(sickNotes);

        when(calendarService.getWorkDays(DayLength.FULL, LocalDate.of(2013, OCTOBER, 7),
            LocalDate.of(2013, OCTOBER, 11), person))
            .thenReturn(new BigDecimal("5"));

        when(calendarService.getWorkDays(DayLength.FULL, LocalDate.of(2013, DECEMBER, 18),
            LocalDate.of(2013, DECEMBER, 31), person))
            .thenReturn(new BigDecimal("9"));

        final Clock fixedClock = Clock.fixed(Instant.parse("2013-04-02T00:00:00.00Z"), clock.getZone());
        statistics = new SickNoteStatistics(fixedClock, sickNoteDAO, calendarService);
    }


    @Test
    public void testGetTotalNumberOfSickNotes() {

        Assert.assertEquals(2, statistics.getTotalNumberOfSickNotes());
    }


    @Test
    public void testGetTotalNumberOfSickDays() {

        Assert.assertEquals(new BigDecimal("14"), statistics.getTotalNumberOfSickDays());
    }


    @Test
    public void testGetAverageDurationOfDiseasePerPerson() {

        // 2 sick notes: 1st with 5 workdays and 2nd with 9 workdays --> sum = 14 workdays
        // 14 workdays / 7 persons = 2 workdays per person
        final Clock fixedClock = Clock.fixed(Instant.parse("2013-04-02T00:00:00.00Z"), clock.getZone());

        statistics = new SickNoteStatistics(fixedClock, sickNoteDAO, calendarService);

        Assert.assertEquals(new BigDecimal("2").setScale(2, RoundingMode.HALF_UP),
            statistics.getAverageDurationOfDiseasePerPerson().setScale(2, RoundingMode.HALF_UP));
    }


    @Test
    public void testGetAverageDurationOfDiseasePerPersonDivisionByZero() {

        final Clock fixedClock = Clock.fixed(Instant.parse("2013-04-02T00:00:00.00Z"), clock.getZone());

        when(sickNoteDAO.getNumberOfPersonsWithMinimumOneSickNote(2013)).thenReturn(0L);

        statistics = new SickNoteStatistics(fixedClock, sickNoteDAO, calendarService);

        Assert.assertEquals(BigDecimal.ZERO, statistics.getAverageDurationOfDiseasePerPerson());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testGetTotalNumberOfSickDaysInvalidDateRange() {

        final Clock fixedClock = Clock.fixed(Instant.parse("2015-04-02T00:00:00.00Z"), clock.getZone());

        when(sickNoteDAO.getAllActiveByYear(2015)).thenReturn(sickNotes);

        statistics = new SickNoteStatistics(fixedClock, sickNoteDAO, calendarService);

        statistics.getTotalNumberOfSickDays();
    }
}
