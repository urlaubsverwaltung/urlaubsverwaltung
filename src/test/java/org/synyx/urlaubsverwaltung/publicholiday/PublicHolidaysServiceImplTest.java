package org.synyx.urlaubsverwaltung.publicholiday;

import de.focus_shift.jollyday.core.HolidayCalendar;
import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.math.BigDecimal.ZERO;
import static java.time.LocalDate.of;
import static java.time.Month.APRIL;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.JULY;
import static java.time.Month.JUNE;
import static java.time.Month.MARCH;
import static java.time.Month.MAY;
import static java.time.Month.NOVEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.BULGARIA;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.CROATIA;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BAYERN_MUENCHEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BERLIN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.LATVIA;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.NONE;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SPAIN_BARCELONA;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SPAIN_LA_RIOJA;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.UNITED_KINGDOM_ENGLAND;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.UNITED_KINGDOM_NORTHERN_IRELAND;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.UNITED_KINGDOM_SCOTLAND;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.UNITED_KINGDOM_WALES;

class PublicHolidaysServiceImplTest {

    private PublicHolidaysService sut;

    @BeforeEach
    void setUp() {
        sut = new PublicHolidaysServiceImpl(Map.of(
            "de", getHolidayManager(HolidayCalendar.GERMANY),
            "hr", getHolidayManager(HolidayCalendar.CROATIA),
            "pt", getHolidayManager(HolidayCalendar.PORTUGAL),
            "bg", getHolidayManager(HolidayCalendar.BULGARIA),
            "lv", getHolidayManager(HolidayCalendar.LATVIA),
            "gb", getHolidayManager(HolidayCalendar.UNITED_KINGDOM),
            "es", getHolidayManager(HolidayCalendar.SPAIN)
        ));
    }

    @Test
    void ensureCorrectWorkingDurationForWorkDay() {
        final LocalDate localDate = of(2013, Month.NOVEMBER, 27);
        final Optional<PublicHoliday> maybePublicHoliday = publicHoliday(localDate, GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureCorrectWorkingDurationForPublicHoliday() {
        final Optional<PublicHoliday> maybePublicHoliday = publicHoliday(of(2013, DECEMBER, 25), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(ZERO));
    }

    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBerlin() {
        final Optional<PublicHoliday> maybePublicHoliday = publicHoliday(of(2015, AUGUST, 15), GERMANY_BERLIN);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBadenWuerttemberg() {
        final Optional<PublicHoliday> maybePublicHoliday = publicHoliday(of(2015, AUGUST, 15), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureCorrectWorkingDurationForAssumptionDayForBayernMuenchen() {
        final Optional<PublicHoliday> maybePublicHoliday = publicHoliday(of(2015, DECEMBER, 15), GERMANY_BAYERN_MUENCHEN);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureGetDayLengthReturnsFullForCorpusChristi() {
        final Optional<PublicHoliday> maybePublicHoliday = publicHoliday(of(2019, MAY, 30), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.FULL));
    }

    @Test
    void ensureGetDayLengthReturnsFullForAssumptionDayInBayernMunich() {
        final Optional<PublicHoliday> maybePublicHoliday = publicHoliday(of(2019, AUGUST, 15), GERMANY_BAYERN_MUENCHEN);
        assertThat(maybePublicHoliday).hasValueSatisfying(publicHoliday -> assertThat(publicHoliday.dayLength()).isEqualTo(DayLength.FULL));
    }

    @Test
    void ensureGetDayLengthReturnsZeroForAssumptionDayInBerlin() {
        final Optional<PublicHoliday> maybePublicHoliday = publicHoliday(of(2019, AUGUST, 15), GERMANY_BERLIN);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureGetDayLengthReturnsZeroForAssumptionDayInBadenWuerttemberg() {
        final Optional<PublicHoliday> maybePublicHoliday = publicHoliday(of(2019, AUGUST, 15), GERMANY_BADEN_WUERTTEMBERG);
        assertThat(maybePublicHoliday).isEmpty();
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
        final Optional<PublicHoliday> maybePublicHoliday = publicHoliday(of(2026, JULY, 1), federalState);
        assertThat(maybePublicHoliday.isPresent()).isEqualTo(isPublicHoliday);
    }

    @ParameterizedTest
    @CsvSource({"PORTUGAL, false", "PORTUGAL_AZORES, true", "PORTUGAL_MADEIRA, false"})
    void ensureAzoresDayIsAPublicHolidayInAzoresOnly(FederalState federalState, boolean isPublicHoliday) {
        // Dia da Região Autónoma dos Açores is on whit monday
        final Optional<PublicHoliday> maybePublicHoliday = publicHoliday(of(2026, MAY, 25), federalState);
        assertThat(maybePublicHoliday.isPresent()).isEqualTo(isPublicHoliday);
    }

    @ParameterizedTest
    @CsvSource({"PORTUGAL", "PORTUGAL_AZORES", "PORTUGAL_MADEIRA"})
    void ensureNationalPublicHolidaysOfPortugalApplyToAllRegions(FederalState federalState) {
        final Optional<PublicHoliday> maybePublicHoliday = publicHoliday(of(2026, APRIL, 25), federalState);
        assertThat(maybePublicHoliday).isPresent();
    }

    private static Stream<Arguments> regionalPublicHolidays() {
        return Stream.of(
            Arguments.of(UNITED_KINGDOM_ENGLAND, of(2026, APRIL, 6)), // Easter Monday
            Arguments.of(UNITED_KINGDOM_WALES, of(2026, APRIL, 6)), // Easter Monday
            Arguments.of(UNITED_KINGDOM_NORTHERN_IRELAND, of(2026, MARCH, 17)), // St Patrick's Day
            Arguments.of(UNITED_KINGDOM_SCOTLAND, of(2026, NOVEMBER, 30)), // St Andrew's Day
            Arguments.of(SPAIN_LA_RIOJA, of(2026, JUNE, 9)), // Day of La Rioja
            Arguments.of(SPAIN_BARCELONA, of(2026, MAY, 25)), // Whit Monday
            Arguments.of(SPAIN_BARCELONA, of(2026, JUNE, 24)) // St John's Day of Catalonia
        );
    }

    @ParameterizedTest
    @MethodSource("regionalPublicHolidays")
    void ensureRegionalPublicHolidayIsFound(FederalState federalState, LocalDate date) {
        assertThat(publicHoliday(date, federalState)).isPresent();
    }

    @Test
    void ensureCarnivalIsNoPublicHolidayInPortugal() {
        // carnival tuesday is only an observance in portugal
        final Optional<PublicHoliday> maybePublicHoliday = publicHoliday(of(2026, FEBRUARY, 17), FederalState.PORTUGAL);
        assertThat(maybePublicHoliday).isEmpty();
    }

    @Test
    void ensureEasterMondayInBulgariaFollowsTheJulianCalendar() {
        assertThat(publicHoliday(of(2026, APRIL, 13), BULGARIA)).isPresent();
        assertThat(publicHoliday(of(2026, APRIL, 6), BULGARIA)).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"2026-05-04", "2026-06-23", "2026-06-24", "2026-11-18"})
    void ensureNationalPublicHolidaysOfLatvia(LocalDate date) {
        assertThat(publicHoliday(date, LATVIA)).isPresent();
    }

    @Test
    void christmasEveIsNoPublicHoliday() {
        final List<PublicHoliday> publicHolidays = sut.getPublicHolidays(of(2026, DECEMBER, 1), of(2026, DECEMBER, 31), GERMANY_BADEN_WUERTTEMBERG);

        assertThat(publicHolidays)
            .extracting(PublicHoliday::date)
            .containsExactlyInAnyOrder(of(2026, DECEMBER, 25), of(2026, DECEMBER, 26));
        assertThat(publicHolidays).extracting(PublicHoliday::dayLength).containsOnly(DayLength.FULL);
    }

    @Test
    void newYearsEveIsNoPublicHolidayWhenFederalStateHasNoPublicHolidays() {
        assertThat(sut.getPublicHolidays(of(2026, DECEMBER, 30), of(2026, DECEMBER, 31), NONE)).isEmpty();
    }

    private Optional<PublicHoliday> publicHoliday(LocalDate date, FederalState federalState) {
        return sut.getPublicHolidays(date, date, federalState).stream().findFirst();
    }

    private HolidayManager getHolidayManager(HolidayCalendar holidayCalendar) {
        return HolidayManager.getInstance(ManagerParameters.create(holidayCalendar));
    }

    @Test
    void ensureThrowsForDateRangeLongerThanTheMaximumCalculatedDuration() {
        final LocalDate from = LocalDate.of(2000, JANUARY, 1);
        final LocalDate to = LocalDate.of(2101, JANUARY, 1);
        assertThatThrownBy(() -> sut.getPublicHolidays(from, to, GERMANY_BADEN_WUERTTEMBERG))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
