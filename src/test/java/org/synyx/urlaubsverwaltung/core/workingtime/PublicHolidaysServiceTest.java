
package org.synyx.urlaubsverwaltung.core.workingtime;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;

import java.io.IOException;

import java.math.BigDecimal;


/**
 * Unit test for {@link PublicHolidaysService}.
 *
 * @author  Aljona Murygina
 */
public class PublicHolidaysServiceTest {

    private static final FederalState state = FederalState.BADEN_WUERTTEMBERG;

    private PublicHolidaysService publicHolidaysService;
    private SettingsService settingsService;

    @Before
    public void setUp() throws IOException {

        settingsService = Mockito.mock(SettingsService.class);
        publicHolidaysService = new PublicHolidaysService(settingsService);

        Mockito.when(settingsService.getSettings()).thenReturn(new Settings());
    }


    @Test
    public void ensureCheckForPublicHolidayReturnsTrueForPublicHoliday() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 25);

        boolean isPublicHoliday = publicHolidaysService.isPublicHoliday(testDate, state);

        Assert.assertTrue("Christmas should be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureCheckForPublicHolidayReturnsFalseForWorkDay() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 20);

        boolean isPublicHoliday = publicHolidaysService.isPublicHoliday(testDate, state);

        Assert.assertFalse("Work day should not be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureCorpusChristiIsRecognizedAsPublicHoliday() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.MAY, 30);

        boolean isPublicHoliday = publicHolidaysService.isPublicHoliday(testDate, state);

        Assert.assertTrue("Corpus Christi should be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureChristmasEveIsRecognizedAsPublicHoliday() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 24);

        boolean isPublicHoliday = publicHolidaysService.isPublicHoliday(testDate, state);

        Assert.assertTrue("Christmas Eve should be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureNewYearsEveIsRecognizedAsPublicHoliday() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        boolean isPublicHoliday = publicHolidaysService.isPublicHoliday(testDate, state);

        Assert.assertTrue("New Years Eve should be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureCorrectWorkingDurationForWorkDay() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.NOVEMBER, 27);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        Assert.assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    public void ensureCorrectWorkingDurationForPublicHoliday() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 25);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        Assert.assertEquals("Wrong working duration", BigDecimal.ZERO, workingDuration);
    }


    @Test
    public void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfFullDay() {

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForChristmasEve(DayLength.FULL);

        Mockito.when(settingsService.getSettings()).thenReturn(settings);

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 24);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        Assert.assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    public void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfFullDay() {

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForNewYearsEve(DayLength.FULL);

        Mockito.when(settingsService.getSettings()).thenReturn(settings);

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        Assert.assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    public void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfMorning() {

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForChristmasEve(DayLength.MORNING);

        Mockito.when(settingsService.getSettings()).thenReturn(settings);

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 24);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        Assert.assertEquals("Wrong working duration", new BigDecimal("0.5"), workingDuration);
    }


    @Test
    public void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfNoon() {

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForNewYearsEve(DayLength.NOON);

        Mockito.when(settingsService.getSettings()).thenReturn(settings);

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        Assert.assertEquals("Wrong working duration", new BigDecimal("0.5"), workingDuration);
    }


    @Test
    public void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfZero() {

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForChristmasEve(DayLength.ZERO);

        Mockito.when(settingsService.getSettings()).thenReturn(settings);

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 24);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        Assert.assertEquals("Wrong working duration", BigDecimal.ZERO, workingDuration);
    }


    @Test
    public void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfZero() {

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForNewYearsEve(DayLength.ZERO);

        Mockito.when(settingsService.getSettings()).thenReturn(settings);

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        Assert.assertEquals("Wrong working duration", BigDecimal.ZERO, workingDuration);
    }


    @Test
    public void ensureAssumptionDayIsAPublicHolidayForBayernMuenchen() {

        boolean isPublicHoliday = publicHolidaysService.isPublicHoliday(new DateMidnight(2015, DateTimeConstants.AUGUST,
                    15), FederalState.BAYERN_MUENCHEN);

        Assert.assertTrue("Assumption Day should be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureAssumptionDayIsNoPublicHolidayForBerlin() {

        boolean isPublicHoliday = publicHolidaysService.isPublicHoliday(new DateMidnight(2015, DateTimeConstants.AUGUST,
                    15), FederalState.BERLIN);

        Assert.assertFalse("Assumption Day should not be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureAssumptionDayIsNoPublicHolidayForBadenWuerttemberg() {

        boolean isPublicHoliday = publicHolidaysService.isPublicHoliday(new DateMidnight(2015, DateTimeConstants.AUGUST,
                    15), FederalState.BADEN_WUERTTEMBERG);

        Assert.assertFalse("Assumption Day should not be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureCorrectWorkingDurationForAssumptionDayForBerlin() {

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(new DateMidnight(2015,
                    DateTimeConstants.AUGUST, 15), FederalState.BERLIN);

        Assert.assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    public void ensureCorrectWorkingDurationForAssumptionDayForBadenWuerttemberg() {

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(new DateMidnight(2015,
                    DateTimeConstants.AUGUST, 15), FederalState.BADEN_WUERTTEMBERG);

        Assert.assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    public void ensureCorrectWorkingDurationForAssumptionDayForBayernMuenchen() {

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(new DateMidnight(2015,
                    DateTimeConstants.AUGUST, 15), FederalState.BAYERN_MUENCHEN);

        Assert.assertEquals("Wrong working duration", BigDecimal.ZERO, workingDuration);
    }
}
