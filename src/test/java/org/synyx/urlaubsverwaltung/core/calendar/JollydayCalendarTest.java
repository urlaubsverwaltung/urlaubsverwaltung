
package org.synyx.urlaubsverwaltung.core.calendar;

import de.jollyday.Holiday;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;

import java.io.IOException;

import java.math.BigDecimal;

import java.util.Properties;
import java.util.Set;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.core.calendar.JollydayCalendar}.
 *
 * @author  Aljona Murygina
 */
public class JollydayCalendarTest {

    private JollydayCalendar jollydayCalendar;
    private SettingsService settingsService;

    @Before
    public void setUp() throws IOException {

        settingsService = Mockito.mock(SettingsService.class);
        jollydayCalendar = new JollydayCalendar(settingsService);

        Mockito.when(settingsService.getSettings()).thenReturn(new Settings());
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
    public void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfFullDay() {

        Settings settings = new Settings();
        settings.setWorkingDurationForChristmasEve(DayLength.FULL);

        Mockito.when(settingsService.getSettings()).thenReturn(settings);

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 24);

        BigDecimal workingDuration = jollydayCalendar.getWorkingDurationOfDate(testDate);

        Assert.assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    public void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfFullDay() {

        Settings settings = new Settings();
        settings.setWorkingDurationForNewYearsEve(DayLength.FULL);

        Mockito.when(settingsService.getSettings()).thenReturn(settings);

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        BigDecimal workingDuration = jollydayCalendar.getWorkingDurationOfDate(testDate);

        Assert.assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    public void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfMorning() {

        Settings settings = new Settings();
        settings.setWorkingDurationForChristmasEve(DayLength.MORNING);

        Mockito.when(settingsService.getSettings()).thenReturn(settings);

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 24);

        BigDecimal workingDuration = jollydayCalendar.getWorkingDurationOfDate(testDate);

        Assert.assertEquals("Wrong working duration", new BigDecimal("0.5"), workingDuration);
    }


    @Test
    public void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfNoon() {

        Settings settings = new Settings();
        settings.setWorkingDurationForNewYearsEve(DayLength.NOON);

        Mockito.when(settingsService.getSettings()).thenReturn(settings);

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        BigDecimal workingDuration = jollydayCalendar.getWorkingDurationOfDate(testDate);

        Assert.assertEquals("Wrong working duration", new BigDecimal("0.5"), workingDuration);
    }


    @Test
    public void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfZero() {

        Settings settings = new Settings();
        settings.setWorkingDurationForChristmasEve(DayLength.ZERO);

        Mockito.when(settingsService.getSettings()).thenReturn(settings);

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 24);

        BigDecimal workingDuration = jollydayCalendar.getWorkingDurationOfDate(testDate);

        Assert.assertEquals("Wrong working duration", BigDecimal.ZERO, workingDuration);
    }


    @Test
    public void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfZero() {

        Settings settings = new Settings();
        settings.setWorkingDurationForNewYearsEve(DayLength.ZERO);

        Mockito.when(settingsService.getSettings()).thenReturn(settings);

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        BigDecimal workingDuration = jollydayCalendar.getWorkingDurationOfDate(testDate);

        Assert.assertEquals("Wrong working duration", BigDecimal.ZERO, workingDuration);
    }


    @Test
    public void ensureAssumptionDayIsAPublicHolidayForBayernMuenchen() {

        Settings settings = new Settings();
        settings.setFederalState(FederalState.BAYERN_MUENCHEN);

        Mockito.when(settingsService.getSettings()).thenReturn(settings);

        boolean isPublicHoliday = jollydayCalendar.isPublicHoliday(new DateMidnight(2015, DateTimeConstants.AUGUST,
                    15));

        Assert.assertTrue("Assumption Day should be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureAssumptionDayIsNoPublicHolidayForBerlin() {

        Settings settings = new Settings();
        settings.setFederalState(FederalState.BERLIN);

        Mockito.when(settingsService.getSettings()).thenReturn(settings);

        boolean isPublicHoliday = jollydayCalendar.isPublicHoliday(new DateMidnight(2015, DateTimeConstants.AUGUST,
                    15));

        Assert.assertFalse("Assumption Day should not be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureAssumptionDayIsNoPublicHolidayForBadenWuerttemberg() {

        Settings settings = new Settings();
        settings.setFederalState(FederalState.BADEN_WUERTTEMBERG);

        Mockito.when(settingsService.getSettings()).thenReturn(settings);

        boolean isPublicHoliday = jollydayCalendar.isPublicHoliday(new DateMidnight(2015, DateTimeConstants.AUGUST,
                    15));

        Assert.assertFalse("Assumption Day should not be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureCorrectWorkingDurationForAssumptionDayForBerlin() {

        Settings settings = new Settings();
        settings.setFederalState(FederalState.BERLIN);

        Mockito.when(settingsService.getSettings()).thenReturn(settings);

        BigDecimal workingDuration = jollydayCalendar.getWorkingDurationOfDate(new DateMidnight(2015,
                    DateTimeConstants.AUGUST, 15));

        Assert.assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    public void ensureCorrectWorkingDurationForAssumptionDayForBadenWuerttemberg() {

        Settings settings = new Settings();
        settings.setFederalState(FederalState.BADEN_WUERTTEMBERG);

        Mockito.when(settingsService.getSettings()).thenReturn(settings);

        BigDecimal workingDuration = jollydayCalendar.getWorkingDurationOfDate(new DateMidnight(2015,
                    DateTimeConstants.AUGUST, 15));

        Assert.assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    public void ensureCorrectWorkingDurationForAssumptionDayForBayernMuenchen() {

        Settings settings = new Settings();
        settings.setFederalState(FederalState.BAYERN_MUENCHEN);

        Mockito.when(settingsService.getSettings()).thenReturn(settings);

        BigDecimal workingDuration = jollydayCalendar.getWorkingDurationOfDate(new DateMidnight(2015,
                    DateTimeConstants.AUGUST, 15));

        Assert.assertEquals("Wrong working duration", BigDecimal.ZERO, workingDuration);
    }
}
