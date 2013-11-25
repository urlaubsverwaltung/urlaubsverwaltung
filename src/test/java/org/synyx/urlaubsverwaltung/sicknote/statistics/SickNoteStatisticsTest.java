package org.synyx.urlaubsverwaltung.sicknote.statistics;

import junit.framework.Assert;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;

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
    private OwnCalendarService calendarService;
    private List<SickNote> sickNotes;

    @Before
    public void setUp() throws Exception {

        calendarService = Mockito.mock(OwnCalendarService.class);
        sickNotes = new ArrayList<SickNote>();

        SickNote sickNote1 = new SickNote();
        sickNote1.setStartDate(new DateMidnight(2013, DateTimeConstants.OCTOBER, 7));
        sickNote1.setEndDate(new DateMidnight(2013, DateTimeConstants.OCTOBER, 11));

        SickNote sickNote2 = new SickNote();
        sickNote2.setStartDate(new DateMidnight(2013, DateTimeConstants.DECEMBER, 18));
        sickNote2.setEndDate(new DateMidnight(2014, DateTimeConstants.JANUARY, 3));

        sickNotes.add(sickNote1);
        sickNotes.add(sickNote2);

        statistics = new SickNoteStatistics(2013, sickNotes, calendarService);

        Mockito.when(calendarService.getWorkDays(DayLength.FULL, new DateMidnight(2013, DateTimeConstants.OCTOBER, 7),
                new DateMidnight(2013, DateTimeConstants.OCTOBER, 11))).thenReturn(new BigDecimal("5"));

        Mockito.when(calendarService.getWorkDays(DayLength.FULL, new DateMidnight(2013, DateTimeConstants.DECEMBER, 18),
                new DateMidnight(2013, DateTimeConstants.DECEMBER, 31))).thenReturn(new BigDecimal("9"));
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
    public void testGetAverageDurationOfDisease() throws Exception {

        // 2 sick notes: 1st with 5 workdays and 2nd with 9 workdays --> sum = 14 workdays

        Assert.assertEquals(new BigDecimal(14 / 2).setScale(2, RoundingMode.HALF_UP),
            statistics.getAverageDurationOfDisease().setScale(2, RoundingMode.HALF_UP));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testGetTotalNumberOfSickDaysInvalidDateRange() throws Exception {

        statistics = new SickNoteStatistics(2015, sickNotes, calendarService);

        statistics.getTotalNumberOfSickDays();
    }
}
