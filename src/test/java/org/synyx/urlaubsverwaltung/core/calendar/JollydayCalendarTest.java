
package org.synyx.urlaubsverwaltung.core.calendar;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;

import java.io.IOException;

import java.math.BigDecimal;

import java.util.Properties;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.core.calendar.JollydayCalendar}.
 *
 * @author  Aljona Murygina
 */
public class JollydayCalendarTest {

    private JollydayCalendar jollydayCalendar;

    @Before
    public void setUp() throws IOException {

        jollydayCalendar = new JollydayCalendar();
    }


    @Test
    public void ensureCheckForPublicHolidayReturnsTrueForPublicHoliday() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 25);

        boolean isPublicHoliday = jollydayCalendar.isPublicHoliday(testDate);

        Assert.assertTrue("Christmas should be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureCheckForPublicHolidayReturnsFalseForWorkDay() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 20);

        boolean isPublicHoliday = jollydayCalendar.isPublicHoliday(testDate);

        Assert.assertFalse("Work day should not be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureCorpusChristiIsRecognizedAsPublicHoliday() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.MAY, 30);

        boolean isPublicHoliday = jollydayCalendar.isPublicHoliday(testDate);

        Assert.assertTrue("Corpus Christi should be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureChristmasEveIsRecognizedAsPublicHoliday() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 24);

        boolean isPublicHoliday = jollydayCalendar.isPublicHoliday(testDate);

        Assert.assertTrue("Christmas Eve should be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureNewYearsEveIsRecognizedAsPublicHoliday() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        boolean isPublicHoliday = jollydayCalendar.isPublicHoliday(testDate);

        Assert.assertTrue("New Years Eve should be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureCorrectWorkingDurationForWorkDay() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.NOVEMBER, 27);

        BigDecimal workingDuration = jollydayCalendar.getWorkingDurationOfDate(testDate);

        Assert.assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    public void ensureCorrectWorkingDurationForPublicHoliday() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 25);

        BigDecimal workingDuration = jollydayCalendar.getWorkingDurationOfDate(testDate);

        Assert.assertEquals("Wrong working duration", BigDecimal.ZERO, workingDuration);
    }


    @Test
    public void ensureCorrectWorkingDurationForChristmasEve() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 24);

        BigDecimal workingDuration = jollydayCalendar.getWorkingDurationOfDate(testDate);

        Assert.assertEquals("Wrong working duration", new BigDecimal("0.5"), workingDuration);
    }


    @Test
    public void ensureCorrectWorkingDurationForNewYearsEve() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        BigDecimal workingDuration = jollydayCalendar.getWorkingDurationOfDate(testDate);

        Assert.assertEquals("Wrong working duration", new BigDecimal("0.5"), workingDuration);
    }


    @Test
    public void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfFullDay() {

        Properties properties = new Properties();
        properties.put("holiday.CHRISTMAS_EVE.vacationDay", DayLength.FULL.name());

        jollydayCalendar = new JollydayCalendar(properties);

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 24);

        BigDecimal workingDuration = jollydayCalendar.getWorkingDurationOfDate(testDate);

        Assert.assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    public void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfFullDay() {

        Properties properties = new Properties();
        properties.put("holiday.NEW_YEARS_EVE.vacationDay", DayLength.FULL.name());

        jollydayCalendar = new JollydayCalendar(properties);

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        BigDecimal workingDuration = jollydayCalendar.getWorkingDurationOfDate(testDate);

        Assert.assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    public void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfMorning() {

        Properties properties = new Properties();
        properties.put("holiday.CHRISTMAS_EVE.vacationDay", DayLength.MORNING.name());

        jollydayCalendar = new JollydayCalendar(properties);

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 24);

        BigDecimal workingDuration = jollydayCalendar.getWorkingDurationOfDate(testDate);

        Assert.assertEquals("Wrong working duration", new BigDecimal("0.5"), workingDuration);
    }


    @Test
    public void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfNoon() {

        Properties properties = new Properties();
        properties.put("holiday.NEW_YEARS_EVE.vacationDay", DayLength.NOON.name());

        jollydayCalendar = new JollydayCalendar(properties);

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        BigDecimal workingDuration = jollydayCalendar.getWorkingDurationOfDate(testDate);

        Assert.assertEquals("Wrong working duration", new BigDecimal("0.5"), workingDuration);
    }


    @Test
    public void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfZero() {

        Properties properties = new Properties();
        properties.put("holiday.CHRISTMAS_EVE.vacationDay", DayLength.ZERO.name());

        jollydayCalendar = new JollydayCalendar(properties);

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 24);

        BigDecimal workingDuration = jollydayCalendar.getWorkingDurationOfDate(testDate);

        Assert.assertEquals("Wrong working duration", BigDecimal.ZERO, workingDuration);
    }


    @Test
    public void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfZero() {

        Properties properties = new Properties();
        properties.put("holiday.NEW_YEARS_EVE.vacationDay", DayLength.ZERO.name());

        jollydayCalendar = new JollydayCalendar(properties);

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        BigDecimal workingDuration = jollydayCalendar.getWorkingDurationOfDate(testDate);

        Assert.assertEquals("Wrong working duration", BigDecimal.ZERO, workingDuration);
    }
}
