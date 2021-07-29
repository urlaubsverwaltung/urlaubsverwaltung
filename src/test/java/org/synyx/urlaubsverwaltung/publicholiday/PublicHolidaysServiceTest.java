package org.synyx.urlaubsverwaltung.publicholiday;

import de.jollyday.HolidayManager;
import de.jollyday.ManagerParameter;
import de.jollyday.ManagerParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.settings.WorkingTimeSettings;
import org.synyx.urlaubsverwaltung.workingtime.settings.WorkingTimeSettingsService;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;

import static java.time.Month.AUGUST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.BADEN_WUERTTEMBERG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.BAYERN_MUENCHEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.BERLIN;

@ExtendWith(MockitoExtension.class)
class PublicHolidaysServiceTest {

    private static final FederalState state = BADEN_WUERTTEMBERG;

    private PublicHolidaysService sut;

    @Mock
    private WorkingTimeSettingsService settingsService;

    @BeforeEach
    void setUp() {
        sut = new PublicHolidaysService(settingsService, getHolidayManager());
    }

    @Test
    void ensureCorrectWorkingDurationForWorkDay() {

        when(settingsService.getSettings()).thenReturn(new WorkingTimeSettings());
        LocalDate testDate = LocalDate.of(2013, Month.NOVEMBER, 27);

        final BigDecimal workingDuration = sut.getWorkingDurationOfDate(testDate, state);
        assertThat(workingDuration).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void ensureCorrectWorkingDurationForPublicHoliday() {

        when(settingsService.getSettings()).thenReturn(new WorkingTimeSettings());
        final LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 25);

        final BigDecimal workingDuration = sut.getWorkingDurationOfDate(testDate, state);
        assertThat(workingDuration).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfFullDay() {

        final WorkingTimeSettings settings = new WorkingTimeSettings();
        settings.setWorkingDurationForChristmasEve(DayLength.FULL);
        when(settingsService.getSettings()).thenReturn(settings);

        LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 24);

        BigDecimal workingDuration = sut.getWorkingDurationOfDate(testDate, state);
        assertThat(workingDuration).isEqualByComparingTo(BigDecimal.ONE);
    }


    @Test
    void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfFullDay() {

        final WorkingTimeSettings settings = new WorkingTimeSettings();
        settings.setWorkingDurationForNewYearsEve(DayLength.FULL);
        when(settingsService.getSettings()).thenReturn(settings);

        LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 31);

        BigDecimal workingDuration = sut.getWorkingDurationOfDate(testDate, state);
        assertThat(workingDuration).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfMorning() {

        final WorkingTimeSettings settings = new WorkingTimeSettings();
        settings.setWorkingDurationForChristmasEve(DayLength.MORNING);
        when(settingsService.getSettings()).thenReturn(settings);

        final LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 24);

        final BigDecimal workingDuration = sut.getWorkingDurationOfDate(testDate, state);
        assertThat(workingDuration).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfNoon() {

        final WorkingTimeSettings settings = new WorkingTimeSettings();
        settings.setWorkingDurationForNewYearsEve(DayLength.NOON);
        when(settingsService.getSettings()).thenReturn(settings);

        final LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 31);

        final BigDecimal workingDuration = sut.getWorkingDurationOfDate(testDate, state);
        assertThat(workingDuration).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfZero() {

        final WorkingTimeSettings settings = new WorkingTimeSettings();
        settings.setWorkingDurationForChristmasEve(DayLength.ZERO);
        when(settingsService.getSettings()).thenReturn(settings);

        final LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 24);

        final BigDecimal workingDuration = sut.getWorkingDurationOfDate(testDate, state);
        assertThat(workingDuration).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfZero() {

        final WorkingTimeSettings settings = new WorkingTimeSettings();
        settings.setWorkingDurationForNewYearsEve(DayLength.ZERO);
        when(settingsService.getSettings()).thenReturn(settings);

        final LocalDate testDate = LocalDate.of(2013, Month.DECEMBER, 31);

        final BigDecimal workingDuration = sut.getWorkingDurationOfDate(testDate, state);
        assertThat(workingDuration).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBerlin() {

        when(settingsService.getSettings()).thenReturn(new WorkingTimeSettings());

        final BigDecimal workingDuration = sut.getWorkingDurationOfDate(LocalDate.of(2015, AUGUST, 15), BERLIN);
        assertThat(workingDuration).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBadenWuerttemberg() {

        when(settingsService.getSettings()).thenReturn(new WorkingTimeSettings());

        final BigDecimal workingDuration = sut.getWorkingDurationOfDate(LocalDate.of(2015, AUGUST, 15), BADEN_WUERTTEMBERG);
        assertThat(workingDuration).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBayernMuenchen() {
        when(settingsService.getSettings()).thenReturn(new WorkingTimeSettings());

        final BigDecimal workingDuration = sut.getWorkingDurationOfDate(LocalDate.of(2015, AUGUST, 15), BAYERN_MUENCHEN);
        assertThat(workingDuration).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void ensureGetAbsenceTypeOfDateReturnsZeroWhenDateIsNoPublicHoliday() {

        when(settingsService.getSettings()).thenReturn(new WorkingTimeSettings());

        final LocalDate date = LocalDate.of(2019, 1, 2);

        final DayLength actual = sut.getAbsenceTypeOfDate(date, BADEN_WUERTTEMBERG);
        assertThat(actual).isEqualTo(DayLength.ZERO);
    }

    @Test
    void ensureGetAbsenceTypeOfDateReturnsFullForCorpusChristi() {

        when(settingsService.getSettings()).thenReturn(new WorkingTimeSettings());
        final LocalDate corpusChristi = LocalDate.of(2019, Month.MAY, 30);

        final DayLength actual = sut.getAbsenceTypeOfDate(corpusChristi, BADEN_WUERTTEMBERG);
        assertThat(actual).isEqualTo(DayLength.FULL);
    }

    @Test
    void ensureGetAbsenceTypeOfDateReturnsFullForAssumptionDayInBayernMunich() {

        when(settingsService.getSettings()).thenReturn(new WorkingTimeSettings());
        final LocalDate assumptionDay = LocalDate.of(2019, AUGUST, 15);

        final DayLength actual = sut.getAbsenceTypeOfDate(assumptionDay, BAYERN_MUENCHEN);
        assertThat(actual).isEqualTo(DayLength.FULL);
    }

    @Test
    void ensureGetAbsenceTypeOfDateReturnsZeroForAssumptionDayInBerlin() {

        when(settingsService.getSettings()).thenReturn(new WorkingTimeSettings());

        final LocalDate assumptionDay = LocalDate.of(2019, AUGUST, 15);

        final DayLength actual = sut.getAbsenceTypeOfDate(assumptionDay, BERLIN);
        assertThat(actual).isEqualTo(DayLength.ZERO);
    }

    @Test
    void ensureGetAbsenceTypeOfDateReturnsZeroForAssumptionDayInBadenWuerttemberg() {

        when(settingsService.getSettings()).thenReturn(new WorkingTimeSettings());

        final LocalDate assumptionDay = LocalDate.of(2019, AUGUST, 15);

        final DayLength actual = sut.getAbsenceTypeOfDate(assumptionDay, BADEN_WUERTTEMBERG);
        assertThat(actual).isEqualTo(DayLength.ZERO);
    }

    @Test
    void ensureGetAbsenceTypeOfDateReturnsForChristmasEve() {

        when(settingsService.getSettings()).thenReturn(new WorkingTimeSettings());

        final LocalDate date = LocalDate.of(2019, 12, 24);

        final DayLength actual = sut.getAbsenceTypeOfDate(date, BADEN_WUERTTEMBERG);
        assertThat(actual).isEqualTo(DayLength.NOON);
    }

    @Test
    void ensureGetAbsenceTypeOfDateReturnsForNewYearsEve() {

        when(settingsService.getSettings()).thenReturn(new WorkingTimeSettings());

        final LocalDate date = LocalDate.of(2019, 12, 31);

        final DayLength actual = sut.getAbsenceTypeOfDate(date, BADEN_WUERTTEMBERG);
        assertThat(actual).isEqualTo(DayLength.NOON);
    }

    private HolidayManager getHolidayManager() {
        final HolidayManager holidayManager;
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final URL url = cl.getResource("Holidays_de.xml");
        final ManagerParameter managerParameter = ManagerParameters.create(url);
        holidayManager = HolidayManager.getInstance(managerParameter);
        return holidayManager;
    }
}
