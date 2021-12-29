package org.synyx.urlaubsverwaltung.publicholiday;

import de.jollyday.HolidayManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import static de.jollyday.ManagerParameters.create;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDate.of;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.MAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.BADEN_WUERTTEMBERG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.BAYERN_MUENCHEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.BERLIN;

@ExtendWith(MockitoExtension.class)
class PublicHolidaysServiceTest {

    private PublicHolidaysService sut;

    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        sut = new PublicHolidaysService(settingsService, getHolidayManager());
    }

    @Test
    void ensureCorrectWorkingDurationForWorkDay() {

        when(settingsService.getSettings()).thenReturn(new Settings());
        final LocalDate localDate = of(2013, Month.NOVEMBER, 27);

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(localDate, BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureCorrectWorkingDurationForPublicHoliday() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2013, DECEMBER, 25), BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(ZERO));
    }

    @Test
    void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfFullDay() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForChristmasEve(DayLength.FULL);
        when(settingsService.getSettings()).thenReturn(settings);

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2013, DECEMBER, 24), BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(ONE));
    }

    @Test
    void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfFullDay() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForNewYearsEve(DayLength.FULL);
        when(settingsService.getSettings()).thenReturn(settings);

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2013, DECEMBER, 31), BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(ONE));
    }

    @Test
    void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfMorning() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForChristmasEve(DayLength.MORNING);
        when(settingsService.getSettings()).thenReturn(settings);

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2013, DECEMBER, 24), BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(BigDecimal.valueOf(0.5)));
    }

    @Test
    void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfNoon() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForNewYearsEve(DayLength.NOON);
        when(settingsService.getSettings()).thenReturn(settings);

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2013, DECEMBER, 31), BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(BigDecimal.valueOf(0.5)));
    }

    @Test
    void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOfZero() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForChristmasEve(DayLength.ZERO);
        when(settingsService.getSettings()).thenReturn(settings);

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2013, DECEMBER, 24), BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(ZERO));
    }

    @Test
    void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOfZero() {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForNewYearsEve(DayLength.ZERO);
        when(settingsService.getSettings()).thenReturn(settings);

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2013, DECEMBER, 31), BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(ZERO));
    }

    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBerlin() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2015, AUGUST, 15), BERLIN);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBadenWuerttemberg() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2015, AUGUST, 15), BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBayernMuenchen() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2015, DECEMBER, 15), BAYERN_MUENCHEN);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureGetDayLengthReturnsFullForCorpusChristi() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, MAY, 30), BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getDayLength()).isEqualTo(DayLength.FULL));
    }

    @Test
    void ensureGetDayLengthReturnsFullForAssumptionDayInBayernMunich() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, AUGUST, 15), BAYERN_MUENCHEN);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getDayLength()).isEqualTo(DayLength.FULL));
    }

    @Test
    void ensureGetDayLengthReturnsZeroForAssumptionDayInBerlin() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, AUGUST, 15), BERLIN);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureGetDayLengthReturnsZeroForAssumptionDayInBadenWuerttemberg() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, AUGUST, 15), BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureGetDayLengthReturnsForChristmasEve() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, DECEMBER, 24), BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getDayLength()).isEqualTo(DayLength.NOON));
    }

    @Test
    void ensureGetDayLengthReturnsForNewYearsEve() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, DECEMBER, 31), BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getDayLength()).isEqualTo(DayLength.NOON));
    }

    private HolidayManager getHolidayManager() {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final URL url = cl.getResource("Holidays_de.xml");
        return HolidayManager.getInstance(create(url));
    }
}
