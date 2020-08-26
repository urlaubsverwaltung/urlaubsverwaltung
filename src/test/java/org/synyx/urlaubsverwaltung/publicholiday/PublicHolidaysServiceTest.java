package org.synyx.urlaubsverwaltung.publicholiday;

import de.jollyday.HolidayManager;
import de.jollyday.ManagerParameter;
import de.jollyday.ManagerParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.settings.FederalState.BADEN_WUERTTEMBERG;
import static org.synyx.urlaubsverwaltung.settings.FederalState.BAYERN_MUENCHEN;
import static org.synyx.urlaubsverwaltung.settings.FederalState.BERLIN;


/**
 * Unit test for {@link PublicHolidaysService}.
 */
class PublicHolidaysServiceTest {

    private static final FederalState state = BADEN_WUERTTEMBERG;

    private PublicHolidaysService publicHolidaysService;
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource("Holidays_de.xml");
        ManagerParameter managerParameter = ManagerParameters.create(url);
        HolidayManager holidayManager = HolidayManager.getInstance(managerParameter);

        settingsService = mock(SettingsService.class);
        publicHolidaysService = new PublicHolidaysService(settingsService, holidayManager);

        when(settingsService.getSettings()).thenReturn(new Settings());
    }

    @Test
    void ensureCorrectWorkingDurationForWorkDay() {

        LocalDate testDate = LocalDate.of(2013, Month.NOVEMBER, 27);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    void ensureCorrectWorkingDurationForPublicHoliday() {

        LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 25);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        assertEquals("Wrong working duration", BigDecimal.ZERO, workingDuration);
    }


    @Test
    void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfFullDay() {

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForChristmasEve(DayLength.FULL);

        when(settingsService.getSettings()).thenReturn(settings);

        LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 24);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfFullDay() {

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForNewYearsEve(DayLength.FULL);

        when(settingsService.getSettings()).thenReturn(settings);

        LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 31);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfMorning() {

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForChristmasEve(DayLength.MORNING);

        when(settingsService.getSettings()).thenReturn(settings);

        LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 24);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        assertEquals("Wrong working duration", new BigDecimal("0.5"), workingDuration);
    }


    @Test
    void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfNoon() {

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForNewYearsEve(DayLength.NOON);

        when(settingsService.getSettings()).thenReturn(settings);

        LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 31);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        assertEquals("Wrong working duration", new BigDecimal("0.5"), workingDuration);
    }


    @Test
    void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfZero() {

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForChristmasEve(DayLength.ZERO);

        when(settingsService.getSettings()).thenReturn(settings);

        LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 24);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        assertEquals("Wrong working duration", BigDecimal.ZERO, workingDuration);
    }


    @Test
    void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfZero() {

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForNewYearsEve(DayLength.ZERO);

        when(settingsService.getSettings()).thenReturn(settings);

        LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 31);

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(testDate, state);

        assertEquals("Wrong working duration", BigDecimal.ZERO, workingDuration);
    }


    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBerlin() {

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(LocalDate.of(2015,
            Month.AUGUST, 15), FederalState.BERLIN);

        assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBadenWuerttemberg() {

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(LocalDate.of(2015,
            Month.AUGUST, 15), BADEN_WUERTTEMBERG);

        assertEquals("Wrong working duration", BigDecimal.ONE.setScale(1), workingDuration);
    }


    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBayernMuenchen() {

        BigDecimal workingDuration = publicHolidaysService.getWorkingDurationOfDate(LocalDate.of(2015,
            Month.AUGUST, 15), FederalState.BAYERN_MUENCHEN);

        assertEquals("Wrong working duration", BigDecimal.ZERO, workingDuration);
    }

    @Test
    void ensureGetAbsenceTypeOfDateReturnsZeroWhenDateIsNoPublicHoliday() {

        final LocalDate date = LocalDate.of(2019, 1, 2);

        DayLength actual = publicHolidaysService.getAbsenceTypeOfDate(date, BADEN_WUERTTEMBERG);
        assertEquals(null, DayLength.ZERO, actual);
    }

    @Test
    void ensureGetAbsenceTypeOfDateReturnsFullForCorpusChristi() {

        final LocalDate corpusChristi = LocalDate.of(2019, Month.MAY, 30);

        DayLength actual = publicHolidaysService.getAbsenceTypeOfDate(corpusChristi, BADEN_WUERTTEMBERG);
        assertEquals(null, DayLength.FULL, actual);
    }

    @Test
    void ensureGetAbsenceTypeOfDateReturnsFullForAussumptionDayInBayernMunich() {

        final LocalDate assumptionDay = LocalDate.of(2019, Month.AUGUST, 15);

        DayLength actual = publicHolidaysService.getAbsenceTypeOfDate(assumptionDay, BAYERN_MUENCHEN);
        assertEquals(null, DayLength.FULL, actual);
    }

    @Test
    void ensureGetAbsenceTypeOfDateReturnsZeroForAussumptionDayInBerlin() {

        final LocalDate assumptionDay = LocalDate.of(2019, Month.AUGUST, 15);

        DayLength actual = publicHolidaysService.getAbsenceTypeOfDate(assumptionDay, BERLIN);
        assertEquals(null, DayLength.ZERO, actual);
    }

    @Test
    void ensureGetAbsenceTypeOfDateReturnsZeroForAussumptionDayInBadenWuerttemberg() {

        final LocalDate assumptionDay = LocalDate.of(2019, Month.AUGUST, 15);

        DayLength actual = publicHolidaysService.getAbsenceTypeOfDate(assumptionDay, BADEN_WUERTTEMBERG);
        assertEquals(null, DayLength.ZERO, actual);
    }

    @Test
    void ensureGetAbsenceTypeOfDateReturnsForChristmasEve() {

        final LocalDate date = LocalDate.of(2019, 12, 24);

        DayLength actual = publicHolidaysService.getAbsenceTypeOfDate(date, BADEN_WUERTTEMBERG);
        assertEquals(null, DayLength.NOON, actual);
    }

    @Test
    void ensureGetAbsenceTypeOfDateReturnsForNewYearsEve() {

        final LocalDate date = LocalDate.of(2019, 12, 31);

        DayLength actual = publicHolidaysService.getAbsenceTypeOfDate(date, BADEN_WUERTTEMBERG);
        assertEquals(null, DayLength.NOON, actual);
    }
}
