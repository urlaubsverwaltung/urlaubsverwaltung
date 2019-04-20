
package org.synyx.urlaubsverwaltung.workingtime;

import de.jollyday.HolidayManager;
import de.jollyday.ManagerParameter;
import de.jollyday.ManagerParameters;
import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.settings.FederalState.BADEN_WUERTTEMBERG;


/**
 * Unit test for {@link PublicHolidaysService}.
 */
public class PublicHolidaysServiceTest {

    private static final FederalState state = BADEN_WUERTTEMBERG;

    private PublicHolidaysService publicHolidaysService;
    private SettingsService settingsService;

    @Before
    public void setUp() {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource("Holidays_de.xml");
        ManagerParameter managerParameter = ManagerParameters.create(url);
        HolidayManager holidayManager = HolidayManager.getInstance(managerParameter);

        settingsService = mock(SettingsService.class);
        publicHolidaysService = new PublicHolidaysService(settingsService, holidayManager);

        when(settingsService.getSettings()).thenReturn(new Settings());
    }


    @Test
    public void ensureCheckForPublicHolidayReturnsTrueForPublicHoliday() {

        LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 25);

        boolean isPublicHoliday = publicHolidaysService.isPublicHoliday(testDate, state);

        assertTrue("Christmas should be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureCheckForPublicHolidayReturnsFalseForWorkDay() {

        LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 20);

        boolean isPublicHoliday = publicHolidaysService.isPublicHoliday(testDate, state);

        assertFalse("Work day should not be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureCorpusChristiIsRecognizedAsPublicHoliday() {

        LocalDate testDate = LocalDate.of(2013, Month.MAY, 30);

        boolean isPublicHoliday = publicHolidaysService.isPublicHoliday(testDate, state);

        assertTrue("Corpus Christi should be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureChristmasEveIsRecognizedAsPublicHoliday() {

        LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 24);

        boolean isPublicHoliday = publicHolidaysService.isPublicHoliday(testDate, state);

        assertTrue("Christmas Eve should be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureNewYearsEveIsRecognizedAsPublicHoliday() {

        LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 31);

        boolean isPublicHoliday = publicHolidaysService.isPublicHoliday(testDate, state);

        assertTrue("New Years Eve should be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureCorrectWorkingDurationForWorkDay() {

        LocalDate testDate = LocalDate.of(2013, Month.NOVEMBER, 27);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    public void ensureCorrectWorkingDurationForPublicHoliday() {

        LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 25);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        assertEquals("Wrong working duration", BigDecimal.ZERO, workingDuration);
    }


    @Test
    public void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfFullDay() {

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForChristmasEve(DayLength.FULL);

        when(settingsService.getSettings()).thenReturn(settings);

        LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 24);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    public void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfFullDay() {

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForNewYearsEve(DayLength.FULL);

        when(settingsService.getSettings()).thenReturn(settings);

        LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 31);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    public void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfMorning() {

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForChristmasEve(DayLength.MORNING);

        when(settingsService.getSettings()).thenReturn(settings);

        LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 24);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        assertEquals("Wrong working duration", new BigDecimal("0.5"), workingDuration);
    }


    @Test
    public void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfNoon() {

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForNewYearsEve(DayLength.NOON);

        when(settingsService.getSettings()).thenReturn(settings);

        LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 31);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        assertEquals("Wrong working duration", new BigDecimal("0.5"), workingDuration);
    }


    @Test
    public void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfZero() {

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForChristmasEve(DayLength.ZERO);

        when(settingsService.getSettings()).thenReturn(settings);

        LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 24);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        assertEquals("Wrong working duration", BigDecimal.ZERO, workingDuration);
    }


    @Test
    public void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfZero() {

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForNewYearsEve(DayLength.ZERO);

        when(settingsService.getSettings()).thenReturn(settings);

        LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 31);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        assertEquals("Wrong working duration", BigDecimal.ZERO, workingDuration);
    }


    @Test
    public void ensureAssumptionDayIsAPublicHolidayForBayernMuenchen() {

        boolean isPublicHoliday = publicHolidaysService.isPublicHoliday(LocalDate.of(2015, Month.AUGUST,
                    15), FederalState.BAYERN_MUENCHEN);

        assertTrue("Assumption Day should be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureAssumptionDayIsNoPublicHolidayForBerlin() {

        boolean isPublicHoliday = publicHolidaysService.isPublicHoliday(LocalDate.of(2015, Month.AUGUST,
                    15), FederalState.BERLIN);

        assertFalse("Assumption Day should not be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureAssumptionDayIsNoPublicHolidayForBadenWuerttemberg() {

        boolean isPublicHoliday = publicHolidaysService.isPublicHoliday(LocalDate.of(2015, Month.AUGUST,
                    15), BADEN_WUERTTEMBERG);

        assertFalse("Assumption Day should not be recognized as public holiday", isPublicHoliday);
    }


    @Test
    public void ensureCorrectWorkingDurationForAssumptionDayForBerlin() {

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(LocalDate.of(2015,
                    Month.AUGUST, 15), FederalState.BERLIN);

        assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    public void ensureCorrectWorkingDurationForAssumptionDayForBadenWuerttemberg() {

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(LocalDate.of(2015,
                    Month.AUGUST, 15), BADEN_WUERTTEMBERG);

        assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    public void ensureCorrectWorkingDurationForAssumptionDayForBayernMuenchen() {

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(LocalDate.of(2015,
                    Month.AUGUST, 15), FederalState.BAYERN_MUENCHEN);

        assertEquals("Wrong working duration", BigDecimal.ZERO, workingDuration);
    }

    @Test
    public void ensureGetAbsenceTypeOfDateReturnsZeroWhenDateIsNoPublicHoliday() {

        final LocalDate date = LocalDate.of(2019, 1, 2);

        DayLength actual = publicHolidaysService.getAbsenceTypeOfDate(date, BADEN_WUERTTEMBERG);
        assertEquals(null, DayLength.ZERO, actual);
    }

    @Test
    public void ensureGetAbsenceTypeOfDateReturnsFullWhenDateIsPublicHoliday() {

        final LocalDate date = LocalDate.of(2019, 1, 1);

        DayLength actual = publicHolidaysService.getAbsenceTypeOfDate(date, BADEN_WUERTTEMBERG);
        assertEquals(null, DayLength.FULL, actual);
    }

    @Test
    public void ensureGetAbsenceTypeOfDateReturnsForChristmasEve() {

        final LocalDate date = LocalDate.of(2019, 12, 24);

        DayLength actual = publicHolidaysService.getAbsenceTypeOfDate(date, BADEN_WUERTTEMBERG);
        assertEquals(null, DayLength.NOON, actual);
    }

    @Test
    public void ensureGetAbsenceTypeOfDateReturnsForNewYearsEve() {

        final LocalDate date = LocalDate.of(2019, 12, 31);

        DayLength actual = publicHolidaysService.getAbsenceTypeOfDate(date, BADEN_WUERTTEMBERG);
        assertEquals(null, DayLength.NOON, actual);
    }
}
