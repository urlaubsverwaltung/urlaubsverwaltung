package org.synyx.urlaubsverwaltung.publicholiday;

import de.focus_shift.jollyday.core.HolidayCalendar;
import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDate.of;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.MAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BAYERN_MUENCHEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BERLIN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.NONE;

@ExtendWith(MockitoExtension.class)
class PublicHolidaysServiceImplTest {

    private PublicHolidaysService sut;

    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        sut = new PublicHolidaysServiceImpl(settingsService, Map.of("de", getHolidayManager()));
    }

    @Test
    void ensureCorrectWorkingDurationForWorkDay() {

        when(settingsService.getSettings()).thenReturn(new Settings());
        final LocalDate localDate = of(2013, Month.NOVEMBER, 27);

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(localDate, GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureCorrectWorkingDurationForPublicHoliday() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2013, DECEMBER, 25), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(ZERO));
    }

    @ParameterizedTest
    @CsvSource({"FULL, 1", "MORNING, 0.5", "NOON, 0.5", "ZERO, 0"})
    void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOf(String dayLength, double workingDuration) {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForChristmasEve(DayLength.valueOf(dayLength));
        when(settingsService.getSettings()).thenReturn(settings);

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2013, DECEMBER, 24), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(BigDecimal.valueOf(workingDuration)));
    }

    @ParameterizedTest
    @CsvSource({"FULL, 1", "MORNING, 0.5", "NOON, 0.5", "ZERO, 0"})
    void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOf(String dayLength, double workingDuration) {

        final Settings settings = new Settings();
        settings.getWorkingTimeSettings().setWorkingDurationForNewYearsEve(DayLength.FULL);
        when(settingsService.getSettings()).thenReturn(settings);

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2013, DECEMBER, 31), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(ONE));
    }


    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBerlin() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2015, AUGUST, 15), GERMANY_BERLIN);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBadenWuerttemberg() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2015, AUGUST, 15), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBayernMuenchen() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2015, DECEMBER, 15), GERMANY_BAYERN_MUENCHEN);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureGetDayLengthReturnsFullForCorpusChristi() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, MAY, 30), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.FULL));
    }

    @Test
    void ensureGetDayLengthReturnsFullForAssumptionDayInBayernMunich() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, AUGUST, 15), GERMANY_BAYERN_MUENCHEN);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.FULL));
    }

    @Test
    void ensureGetDayLengthReturnsZeroForAssumptionDayInBerlin() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, AUGUST, 15), GERMANY_BERLIN);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureGetDayLengthReturnsZeroForAssumptionDayInBadenWuerttemberg() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, AUGUST, 15), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureGetDayLengthReturnsForChristmasEve() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, DECEMBER, 24), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.NOON));
    }

    @Test
    void ensureGetDayLengthReturnsForNewYearsEve() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, DECEMBER, 31), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.NOON));
    }

    @Test
    void ensureGetPublicHolidaysReturnsForNewYearsEve() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final List<PublicHoliday> publicHolidays = sut.getPublicHolidays(of(2019, DECEMBER, 30), of(2019, DECEMBER, 31), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(publicHolidays)
            .hasSize(1)
            .extracting(PublicHoliday::dayLength)
            .containsExactly(DayLength.NOON);
    }

    @Test
    void ensureGetPublicHolidaysReturnsChristmasForMultipleYears() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final List<PublicHoliday> publicHolidays = sut.getPublicHolidays(of(2020, JANUARY, 1), of(2023, DECEMBER, 31), GERMANY_BADEN_WUERTTEMBERG);

        assertThat(publicHolidays).contains(
            new PublicHoliday(LocalDate.of(2020, DECEMBER, 24), null, null),
            new PublicHoliday(LocalDate.of(2021, DECEMBER, 24), null, null),
            new PublicHoliday(LocalDate.of(2023, DECEMBER, 24), null, null));
    }

    @Test
    void ensureGetPublicHolidaysReturnsNewYearsEveForMultipleYears() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final List<PublicHoliday> publicHolidays = sut.getPublicHolidays(of(2020, JANUARY, 1), of(2023, DECEMBER, 31), GERMANY_BADEN_WUERTTEMBERG);

        assertThat(publicHolidays).contains(
            new PublicHoliday(LocalDate.of(2020, DECEMBER, 31), null, null),
            new PublicHoliday(LocalDate.of(2021, DECEMBER, 31), null, null),
            new PublicHoliday(LocalDate.of(2023, DECEMBER, 31), null, null));
    }

    @Test
    void ensureGetPublicHolidaysReturnsWhenPersonHasNoPublicHolidaysDefined() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final List<PublicHoliday> publicHolidays = sut.getPublicHolidays(of(2020, JANUARY, 1), of(2023, DECEMBER, 31), NONE);

        assertThat(publicHolidays).contains(
            new PublicHoliday(LocalDate.of(2020, DECEMBER, 31), null, null),
            new PublicHoliday(LocalDate.of(2021, DECEMBER, 31), null, null),
            new PublicHoliday(LocalDate.of(2023, DECEMBER, 31), null, null));
    }

    private HolidayManager getHolidayManager() {
        return HolidayManager.getInstance(ManagerParameters.create(HolidayCalendar.GERMANY));
    }
}
