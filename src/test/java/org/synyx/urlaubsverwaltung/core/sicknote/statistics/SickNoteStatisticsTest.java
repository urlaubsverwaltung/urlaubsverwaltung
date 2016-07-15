package org.synyx.urlaubsverwaltung.core.sicknote.statistics;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteDAO;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.ArrayList;
import java.util.List;


/**
 * Unit test for {@link SickNoteStatistics}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SickNoteStatisticsTest {

    private SickNoteStatistics statistics;
    private WorkDaysService calendarService;
    private SickNoteDAO sickNoteDAO;
    private List<SickNote> sickNotes;

    @Before
    public void setUp() throws Exception {

        calendarService = Mockito.mock(WorkDaysService.class);
        sickNoteDAO = Mockito.mock(SickNoteDAO.class);
        sickNotes = new ArrayList<>();

        Person person = TestDataCreator.createPerson();

        SickNote sickNote1 = TestDataCreator.createSickNote(person,
                new DateMidnight(2013, DateTimeConstants.OCTOBER, 7),
                new DateMidnight(2013, DateTimeConstants.OCTOBER, 11), DayLength.FULL);

        SickNote sickNote2 = TestDataCreator.createSickNote(person,
                new DateMidnight(2013, DateTimeConstants.DECEMBER, 18),
                new DateMidnight(2014, DateTimeConstants.JANUARY, 3), DayLength.FULL);

        sickNotes.add(sickNote1);
        sickNotes.add(sickNote2);

        Mockito.when(sickNoteDAO.findNumberOfPersonsWithMinimumOneSickNote(2013)).thenReturn(7L);
        Mockito.when(sickNoteDAO.findAllActiveByYear(2013)).thenReturn(sickNotes);

        Mockito.when(calendarService.getWorkDays(DayLength.FULL, new DateMidnight(2013, DateTimeConstants.OCTOBER, 7),
                    new DateMidnight(2013, DateTimeConstants.OCTOBER, 11), person))
            .thenReturn(new BigDecimal("5"));

        Mockito.when(calendarService.getWorkDays(DayLength.FULL, new DateMidnight(2013, DateTimeConstants.DECEMBER, 18),
                    new DateMidnight(2013, DateTimeConstants.DECEMBER, 31), person))
            .thenReturn(new BigDecimal("9"));

        statistics = new SickNoteStatistics(2013, sickNoteDAO, calendarService);
    }


    @Test
    public void testGetTotalNumberOfSickNotes() throws Exception {

        Assert.assertEquals(2, statistics.getTotalNumberOfSickNotes());
    }


    @Test
    public void testGetTotalNumberOfSickDays() throws Exception {

        Assert.assertEquals(new BigDecimal("14"), statistics.getTotalNumberOfSickDays());
    }


    @Test
    public void testGetAverageDurationOfDiseasePerPerson() throws Exception {

        // 2 sick notes: 1st with 5 workdays and 2nd with 9 workdays --> sum = 14 workdays
        // 14 workdays / 7 persons = 2 workdays per person

        statistics = new SickNoteStatistics(2013, sickNoteDAO, calendarService);

        Assert.assertEquals(new BigDecimal("2").setScale(2, RoundingMode.HALF_UP),
            statistics.getAverageDurationOfDiseasePerPerson().setScale(2, RoundingMode.HALF_UP));
    }


    @Test
    public void testGetAverageDurationOfDiseasePerPersonDivisionByZero() throws Exception {

        Mockito.when(sickNoteDAO.findNumberOfPersonsWithMinimumOneSickNote(2013)).thenReturn(0L);

        statistics = new SickNoteStatistics(2013, sickNoteDAO, calendarService);

        Assert.assertEquals(BigDecimal.ZERO, statistics.getAverageDurationOfDiseasePerPerson());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testGetTotalNumberOfSickDaysInvalidDateRange() throws Exception {

        Mockito.when(sickNoteDAO.findAllActiveByYear(2015)).thenReturn(sickNotes);

        statistics = new SickNoteStatistics(2015, sickNoteDAO, calendarService);

        statistics.getTotalNumberOfSickDays();
    }
}
