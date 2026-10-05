package org.synyx.urlaubsverwaltung.publicholiday;

import de.focus_shift.jollyday.core.HolidayCalendar;
import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.math.BigDecimal.ZERO;
import static java.time.LocalDate.of;
import static java.time.Month.APRIL;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.JULY;
import static java.time.Month.MAY;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.BULGARIA;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.CROATIA;
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
        sut = new PublicHolidaysServiceImpl(settingsService, Map.of(
            "de", getHolidayManager(HolidayCalendar.GERMANY),
            "hr", getHolidayManager(HolidayCalendar.CROATIA),
            "pt", getHolidayManager(HolidayCalendar.PORTUGAL),
            "bg", getHolidayManager(HolidayCalendar.BULGARIA)
        ));
    }

    @Test
    void ensureCorrectWorkingDurationForWorkDay() {
        final LocalDate localDate = of(2013, Month.NOVEMBER, 27);
        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(localDate, GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureCorrectWorkingDurationForPublicHoliday() {
        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2013, DECEMBER, 25), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(ZERO));
    }

    @ParameterizedTest
    @CsvSource({"FULL, 1", "MORNING, 0.5", "NOON, 0.5", "ZERO, 0"})
    void ensureWorkingDurationForChristmasEveCanBeConfiguredToAWorkingDurationOf(String dayLength, double workingDuration) {

        final Settings settings = new Settings();
        settings.getPublicHolidaysSettings().setWorkingDurationForChristmasEve(DayLength.valueOf(dayLength));
        when(settingsService.getSettings()).thenReturn(settings);

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2013, DECEMBER, 24), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(BigDecimal.valueOf(workingDuration)));
    }

    @ParameterizedTest
    @CsvSource({"FULL, 1", "MORNING, 0.5", "NOON, 0.5", "ZERO, 0"})
    void ensureWorkingDurationForNewYearsEveCanBeConfiguredToAWorkingDurationOf(String dayLength, double workingDuration) {

        final Settings settings = new Settings();
        settings.getPublicHolidaysSettings().setWorkingDurationForNewYearsEve(DayLength.valueOf(dayLength));
        when(settingsService.getSettings()).thenReturn(settings);

        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2013, DECEMBER, 31), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(BigDecimal.valueOf(workingDuration)));
    }


    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBerlin() {
        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2015, AUGUST, 15), GERMANY_BERLIN);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBadenWuerttemberg() {
        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2015, AUGUST, 15), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBayernMuenchen() {
        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2015, DECEMBER, 15), GERMANY_BAYERN_MUENCHEN);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureGetDayLengthReturnsFullForCorpusChristi() {
        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, MAY, 30), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.FULL));
    }

    @Test
    void ensureGetDayLengthReturnsFullForAssumptionDayInBayernMunich() {
        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, AUGUST, 15), GERMANY_BAYERN_MUENCHEN);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.FULL));
    }

    @Test
    void ensureGetDayLengthReturnsZeroForAssumptionDayInBerlin() {
        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2019, AUGUST, 15), GERMANY_BERLIN);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureGetDayLengthReturnsZeroForAssumptionDayInBadenWuerttemberg() {
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
    void ensureGetPublicHolidaysReturnsBothPublicHolidaysWhenTwoAreOnTheSameDay() {

        // in croatia there are two public holidays on 2024-05-30 (statehood day and corpus christi)
        final List<PublicHoliday> publicHolidays = sut.getPublicHolidays(of(2024, MAY, 30), of(2024, MAY, 30), CROATIA);

        assertThat(publicHolidays)
            .hasSize(2)
            .allSatisfy(publicHoliday -> assertThat(publicHoliday.date()).isEqualTo(of(2024, MAY, 30)))
            .extracting(PublicHoliday::description)
            .doesNotHaveDuplicates()
            .doesNotContainNull();
    }

    @ParameterizedTest
    @CsvSource({"PORTUGAL, false", "PORTUGAL_AZORES, false", "PORTUGAL_MADEIRA, true"})
    void ensureMadeiraDayIsAPublicHolidayInMadeiraOnly(FederalState federalState, boolean isPublicHoliday) {
        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2026, JULY, 1), federalState);
        assertThat(maybePublicHoliday.isPresent()).isEqualTo(isPublicHoliday);
    }

    @ParameterizedTest
    @CsvSource({"PORTUGAL, false", "PORTUGAL_AZORES, true", "PORTUGAL_MADEIRA, false"})
    void ensureAzoresDayIsAPublicHolidayInAzoresOnly(FederalState federalState, boolean isPublicHoliday) {
        // Dia da Região Autónoma dos Açores is on whit monday
        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2026, MAY, 25), federalState);
        assertThat(maybePublicHoliday.isPresent()).isEqualTo(isPublicHoliday);
    }

    @ParameterizedTest
    @CsvSource({"PORTUGAL", "PORTUGAL_AZORES", "PORTUGAL_MADEIRA"})
    void ensureNationalPublicHolidaysOfPortugalApplyToAllRegions(FederalState federalState) {
        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2026, APRIL, 25), federalState);
        assertThat(maybePublicHoliday).isPresent();
    }

    @Test
    void ensureCarnivalIsNoPublicHolidayInPortugal() {
        // carnival tuesday is only an observance in portugal
        final Optional<PublicHoliday> maybePublicHoliday = sut.getPublicHoliday(of(2026, FEBRUARY, 17), FederalState.PORTUGAL);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureEasterMondayInBulgariaFollowsTheJulianCalendar() {
        assertThat(sut.getPublicHoliday(of(2026, APRIL, 13), BULGARIA)).isPresent();
        assertThat(sut.getPublicHoliday(of(2026, APRIL, 6), BULGARIA)).isEmpty();
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

    @ParameterizedTest
    @EnumSource(value = FederalState.class, names = {"GERMANY_BADEN_WUERTTEMBERG", "GERMANY_BAYERN_MUENCHEN", "GERMANY_BERLIN", "AUSTRIA_WIEN", "SWITZERLAND_ZUERICH", "CROATIA"})
    void ensureGetPublicHolidaysOfARangeEqualsGetPublicHolidayForEveryDateOfTheRange(FederalState federalState) {

        final PublicHolidaysService sut = new PublicHolidaysServiceImpl(settingsService, Map.of(
            "de", getHolidayManager(HolidayCalendar.GERMANY),
            "at", getHolidayManager(HolidayCalendar.AUSTRIA),
            "ch", getHolidayManager(HolidayCalendar.SWITZERLAND),
            "hr", getHolidayManager(HolidayCalendar.CROATIA)
        ));

        final PublicHolidaysSettings publicHolidaysSettings = new PublicHolidaysSettings();
        publicHolidaysSettings.setWorkingDurationForChristmasEve(DayLength.MORNING);
        publicHolidaysSettings.setWorkingDurationForNewYearsEve(DayLength.NOON);
        final Supplier<PublicHolidaysSettings> publicHolidaysSettingsSupplier = () -> publicHolidaysSettings;

        for (int year = 2024; year <= 2026; year++) {
            final LocalDate start = of(year, JANUARY, 1);
            final LocalDate end = of(year, DECEMBER, 31);

            final Map<LocalDate, PublicHoliday> publicHolidaysByDate = sut.getPublicHolidays(start, end, federalState, publicHolidaysSettingsSupplier).stream()
                .collect(toMap(PublicHoliday::date, identity(), (first, _) -> first));

            for (LocalDate date : new DateRange(start, end)) {
                final Optional<PublicHoliday> expected = sut.getPublicHoliday(date, federalState, publicHolidaysSettingsSupplier);
                final Optional<PublicHoliday> actual = Optional.ofNullable(publicHolidaysByDate.get(date));

                // PublicHoliday#equals only compares the date, so compare the day length explicitly
                assertThat(actual.map(PublicHoliday::date)).as("date %s", date).isEqualTo(expected.map(PublicHoliday::date));
                assertThat(actual.map(PublicHoliday::dayLength)).as("day length %s", date).isEqualTo(expected.map(PublicHoliday::dayLength));
            }
        }
    }

    private HolidayManager getHolidayManager(HolidayCalendar holidayCalendar) {
        return HolidayManager.getInstance(ManagerParameters.create(holidayCalendar));
    }
}
