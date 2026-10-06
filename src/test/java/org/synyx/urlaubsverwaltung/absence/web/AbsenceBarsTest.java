package org.synyx.urlaubsverwaltung.absence.web;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.absence.DateRange;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.absence.web.AbsenceBars.Half.FULL;
import static org.synyx.urlaubsverwaltung.absence.web.AbsenceBars.Half.MORNING;
import static org.synyx.urlaubsverwaltung.absence.web.AbsenceBars.Half.NOON;
import static org.synyx.urlaubsverwaltung.absence.web.AbsenceBars.Kind.BRIDGE;
import static org.synyx.urlaubsverwaltung.absence.web.AbsenceBars.Kind.SOLID;

class AbsenceBarsTest {

    private static final AbsenceBars.Absence VACATION_7 = absence("VACATION-7");
    private static final AbsenceBars.Absence VACATION_8 = absence("VACATION-8");
    private static final AbsenceBars.Absence SICK_7 = absence("SICK-7");
    private static final AbsenceBars.Gap NO_WORKDAY = new AbsenceBars.Gap("Kein Arbeitstag");
    private static final AbsenceBars.Gap HOLIDAY = new AbsenceBars.Gap("Feiertag");

    // January 2025: Wed 1st, weekends 4th/5th and 11th/12th, Mon 6th and Mon 13th
    private static final DateRange JANUARY_2025 = new DateRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
    private static final DateRange FEBRUARY_2025 = new DateRange(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 28));
    private static final DateRange DECEMBER_2024 = new DateRange(LocalDate.of(2024, 12, 1), LocalDate.of(2024, 12, 31));
    private static final DateRange YEAR_2024 = new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
    private static final DateRange YEAR_2025 = new DateRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

    @Test
    void singleDay() {
        final Map<LocalDate, List<AbsenceBars.Piece>> pieces = compute(Map.of(jan(7), full(VACATION_7)), JANUARY_2025);

        assertThat(pieces).containsOnlyKeys(jan(7));
        assertThat(pieces.get(jan(7))).containsExactly(solid(FULL, true, true, VACATION_7, 2));
    }

    @Test
    void severalDays() {
        final Map<LocalDate, List<AbsenceBars.Piece>> pieces = compute(Map.of(
            jan(6), full(VACATION_7),
            jan(7), full(VACATION_7),
            jan(8), full(VACATION_7)
        ), JANUARY_2025);

        assertThat(pieces.get(jan(6))).containsExactly(solid(FULL, true, false, VACATION_7, 6));
        assertThat(pieces.get(jan(7))).containsExactly(solid(FULL, false, false, VACATION_7, 0));
        assertThat(pieces.get(jan(8))).containsExactly(solid(FULL, false, true, VACATION_7, 0));
    }

    @Test
    void morningOnly() {
        final Map<LocalDate, List<AbsenceBars.Piece>> pieces = compute(Map.of(
            jan(7), new AbsenceBars.Day(new AbsenceBars.HalfDay(VACATION_7, null), AbsenceBars.HalfDay.EMPTY)
        ), JANUARY_2025);

        assertThat(pieces.get(jan(7))).containsExactly(solid(MORNING, true, true, VACATION_7, 0));
    }

    @Test
    void noonOnly() {
        final Map<LocalDate, List<AbsenceBars.Piece>> pieces = compute(Map.of(
            jan(7), new AbsenceBars.Day(AbsenceBars.HalfDay.EMPTY, new AbsenceBars.HalfDay(VACATION_7, null))
        ), JANUARY_2025);

        assertThat(pieces.get(jan(7))).containsExactly(solid(NOON, true, true, VACATION_7, 0));
    }

    @Test
    void weekendInsideAVacationIsABridge() {
        final Map<LocalDate, List<AbsenceBars.Piece>> pieces = compute(Map.of(
            jan(10), full(VACATION_7),
            jan(11), gap(NO_WORKDAY),
            jan(12), gap(NO_WORKDAY),
            jan(13), full(VACATION_7)
        ), JANUARY_2025);

        assertThat(pieces.get(jan(10))).containsExactly(solid(FULL, true, false, VACATION_7, 2));
        assertThat(pieces.get(jan(11))).containsExactly(bridge(FULL, false, false, VACATION_7, NO_WORKDAY, false));
        assertThat(pieces.get(jan(12))).containsExactly(bridge(FULL, false, false, VACATION_7, NO_WORKDAY, false));
        assertThat(pieces.get(jan(13))).containsExactly(solid(FULL, false, true, VACATION_7, 0));
    }

    @Test
    void holidayInsideAVacationStaysVisibleAsBridge() {
        final Map<LocalDate, List<AbsenceBars.Piece>> pieces = compute(Map.of(
            jan(7), full(VACATION_7),
            jan(8), gap(HOLIDAY),
            jan(9), full(VACATION_7)
        ), JANUARY_2025);

        assertThat(pieces.get(jan(8))).containsExactly(bridge(FULL, false, false, VACATION_7, HOLIDAY, false));
    }

    @Test
    void halfHolidayOnChristmasEveBridgesOnlyTheNoon() {
        final AbsenceBars.Gap christmasEve = new AbsenceBars.Gap("Heiligabend");
        final AbsenceBars.Gap christmas = new AbsenceBars.Gap("Weihnachten");

        final Map<LocalDate, List<AbsenceBars.Piece>> pieces = compute(Map.of(
            dec(23), full(VACATION_7),
            dec(24), new AbsenceBars.Day(new AbsenceBars.HalfDay(VACATION_7, null), new AbsenceBars.HalfDay(null, christmasEve)),
            dec(25), gap(christmas),
            dec(26), gap(christmas),
            dec(27), full(VACATION_7)
        ), DECEMBER_2024);

        assertThat(pieces.get(dec(23))).containsExactly(solid(FULL, true, false, VACATION_7, 3));
        assertThat(pieces.get(dec(24))).containsExactly(
            solid(MORNING, false, false, VACATION_7, 0),
            bridge(NOON, false, false, VACATION_7, christmasEve, false)
        );
        assertThat(pieces.get(dec(27))).containsExactly(solid(FULL, false, true, VACATION_7, 0));
    }

    @Test
    void backToBackApplicationsAreTwoBars() {
        final Map<LocalDate, List<AbsenceBars.Piece>> pieces = compute(Map.of(
            jan(6), full(VACATION_7),
            jan(7), full(VACATION_7),
            jan(8), full(VACATION_8),
            jan(9), full(VACATION_8)
        ), JANUARY_2025);

        assertThat(pieces.get(jan(7))).containsExactly(solid(FULL, false, true, VACATION_7, 0));
        assertThat(pieces.get(jan(8))).containsExactly(solid(FULL, true, false, VACATION_8, 4));
    }

    @Test
    void morningAndNoonOfDifferentAbsencesAreTwoPieces() {
        final Map<LocalDate, List<AbsenceBars.Piece>> pieces = compute(Map.of(
            jan(7), new AbsenceBars.Day(new AbsenceBars.HalfDay(VACATION_7, null), new AbsenceBars.HalfDay(VACATION_8, null))
        ), JANUARY_2025);

        assertThat(pieces.get(jan(7))).containsExactly(
            solid(MORNING, true, true, VACATION_7, 0),
            solid(NOON, true, true, VACATION_8, 0)
        );
    }

    @Test
    void sickNoteOverTheWeekendIsABridgeCoveredByTheSickNote() {
        final Map<LocalDate, List<AbsenceBars.Piece>> pieces = compute(Map.of(
            jan(10), full(SICK_7),
            jan(11), fullWithGap(SICK_7, NO_WORKDAY),
            jan(12), fullWithGap(SICK_7, NO_WORKDAY),
            jan(13), full(SICK_7)
        ), JANUARY_2025);

        assertThat(pieces.get(jan(11))).containsExactly(bridge(FULL, false, false, SICK_7, NO_WORKDAY, true));
        assertThat(pieces.get(jan(13))).containsExactly(solid(FULL, false, true, SICK_7, 0));
    }

    @Test
    void gapsBeforeAndAfterAnAbsenceDoNotBelongToTheBar() {
        final Map<LocalDate, List<AbsenceBars.Piece>> pieces = compute(Map.of(
            jan(4), gap(NO_WORKDAY),
            jan(5), gap(NO_WORKDAY),
            jan(6), full(VACATION_7),
            jan(10), full(VACATION_7),
            jan(7), full(VACATION_7),
            jan(8), full(VACATION_7),
            jan(9), full(VACATION_7),
            jan(11), gap(NO_WORKDAY),
            jan(12), gap(NO_WORKDAY)
        ), JANUARY_2025);

        assertThat(pieces).containsOnlyKeys(jan(6), jan(7), jan(8), jan(9), jan(10));
        assertThat(pieces.get(jan(6))).containsExactly(solid(FULL, true, false, VACATION_7, 10));
        assertThat(pieces.get(jan(10))).containsExactly(solid(FULL, false, true, VACATION_7, 0));
    }

    @Test
    void absenceDirectlyAfterANonWorkdayIsShown() {
        final Map<LocalDate, List<AbsenceBars.Piece>> pieces = compute(Map.of(
            jan(11), gap(NO_WORKDAY),
            jan(12), gap(NO_WORKDAY),
            jan(13), full(VACATION_7)
        ), JANUARY_2025);

        assertThat(pieces).containsOnlyKeys(jan(13));
        assertThat(pieces.get(jan(13))).containsExactly(solid(FULL, true, true, VACATION_7, 2));
    }

    @Test
    void workdayWithoutAbsenceSplitsTheAbsenceIntoTwoBars() {
        final Map<LocalDate, List<AbsenceBars.Piece>> pieces = compute(Map.of(
            jan(6), full(VACATION_7),
            jan(8), full(VACATION_7)
        ), JANUARY_2025);

        assertThat(pieces.get(jan(6))).containsExactly(solid(FULL, true, true, VACATION_7, 2));
        assertThat(pieces.get(jan(8))).containsExactly(solid(FULL, true, true, VACATION_7, 2));
    }

    @Test
    void barCutAtTheMonthEndIsSquareAndRepeatsItsLabel() {
        final Map<LocalDate, AbsenceBars.Day> days = Map.of(
            jan(30), full(VACATION_7),
            jan(31), full(VACATION_7),
            feb(1), gap(NO_WORKDAY),
            feb(2), gap(NO_WORKDAY),
            feb(3), full(VACATION_7),
            feb(4), full(VACATION_7)
        );

        final Map<LocalDate, List<AbsenceBars.Piece>> january = compute(days, JANUARY_2025);
        assertThat(january).containsOnlyKeys(jan(30), jan(31));
        assertThat(january.get(jan(30))).containsExactly(solid(FULL, true, false, VACATION_7, 4));
        assertThat(january.get(jan(31))).containsExactly(solid(FULL, false, false, VACATION_7, 0));

        final Map<LocalDate, List<AbsenceBars.Piece>> february = compute(days, FEBRUARY_2025);
        assertThat(february.get(feb(1))).containsExactly(bridge(FULL, false, false, VACATION_7, NO_WORKDAY, false));
        assertThat(february.get(feb(3))).containsExactly(solid(FULL, false, false, VACATION_7, 4));
        assertThat(february.get(feb(4))).containsExactly(solid(FULL, false, true, VACATION_7, 0));

        final Map<LocalDate, List<AbsenceBars.Piece>> wholeYear = compute(days, YEAR_2025);
        assertThat(wholeYear.get(jan(30))).containsExactly(solid(FULL, true, false, VACATION_7, 4));
        assertThat(wholeYear.get(jan(31))).containsExactly(solid(FULL, false, false, VACATION_7, 0));
        assertThat(wholeYear.get(feb(3))).containsExactly(solid(FULL, false, false, VACATION_7, 4));
    }

    @Test
    void barCrossingTheYearBoundaryIsNotRoundedInDecember() {
        final Map<LocalDate, List<AbsenceBars.Piece>> pieces = compute(Map.of(
            dec(30), full(VACATION_7),
            dec(31), full(VACATION_7),
            jan(1), gap(HOLIDAY),
            jan(2), full(VACATION_7)
        ), YEAR_2024);

        assertThat(pieces).containsOnlyKeys(dec(30), dec(31));
        assertThat(pieces.get(dec(30))).containsExactly(solid(FULL, true, false, VACATION_7, 4));
        assertThat(pieces.get(dec(31))).containsExactly(solid(FULL, false, false, VACATION_7, 0));
    }

    @Test
    void partTimeWeekIsOneBarWithALongBridge() {
        final Map<LocalDate, List<AbsenceBars.Piece>> pieces = compute(Map.of(
            jan(6), full(VACATION_7),
            jan(7), gap(NO_WORKDAY),
            jan(8), gap(NO_WORKDAY),
            jan(9), gap(NO_WORKDAY),
            jan(10), gap(NO_WORKDAY),
            jan(11), gap(NO_WORKDAY),
            jan(12), gap(NO_WORKDAY),
            jan(13), full(VACATION_7)
        ), JANUARY_2025);

        assertThat(pieces.get(jan(6))).containsExactly(solid(FULL, true, false, VACATION_7, 2));
        for (int day = 7; day <= 12; day++) {
            assertThat(pieces.get(jan(day))).containsExactly(bridge(FULL, false, false, VACATION_7, NO_WORKDAY, false));
        }
        assertThat(pieces.get(jan(13))).containsExactly(solid(FULL, false, true, VACATION_7, 0));
    }

    @Test
    void sickNoteOnlyOnNonWorkdaysIsABridgeOnlyBar() {
        final Map<LocalDate, List<AbsenceBars.Piece>> pieces = compute(Map.of(
            jan(11), fullWithGap(SICK_7, NO_WORKDAY),
            jan(12), fullWithGap(SICK_7, NO_WORKDAY)
        ), JANUARY_2025);

        assertThat(pieces.get(jan(11))).containsExactly(bridge(FULL, true, false, SICK_7, NO_WORKDAY, true));
        assertThat(pieces.get(jan(12))).containsExactly(bridge(FULL, false, true, SICK_7, NO_WORKDAY, true));
    }

    @Test
    void labelSkipsAStretchOfASingleHalfDay() {
        final Map<LocalDate, List<AbsenceBars.Piece>> pieces = compute(Map.of(
            jan(10), new AbsenceBars.Day(AbsenceBars.HalfDay.EMPTY, new AbsenceBars.HalfDay(VACATION_7, null)),
            jan(11), gap(NO_WORKDAY),
            jan(12), gap(NO_WORKDAY),
            jan(13), full(VACATION_7)
        ), JANUARY_2025);

        assertThat(pieces.get(jan(10))).containsExactly(solid(NOON, true, false, VACATION_7, 0));
        assertThat(pieces.get(jan(13))).containsExactly(solid(FULL, false, true, VACATION_7, 2));
    }

    @Test
    void vacationStartingOnAHalfHolidayIsLabelledOnItsFirstWholeDay() {
        final AbsenceBars.Gap christmasEve = new AbsenceBars.Gap("Heiligabend");
        final AbsenceBars.Gap christmas = new AbsenceBars.Gap("Weihnachten");

        final Map<LocalDate, List<AbsenceBars.Piece>> pieces = compute(Map.of(
            dec(24), new AbsenceBars.Day(new AbsenceBars.HalfDay(VACATION_7, null), new AbsenceBars.HalfDay(null, christmasEve)),
            dec(25), gap(christmas),
            dec(26), gap(christmas),
            dec(27), full(VACATION_7),
            dec(28), gap(NO_WORKDAY),
            dec(29), gap(NO_WORKDAY),
            dec(30), full(VACATION_7),
            dec(31), full(VACATION_7)
        ), DECEMBER_2024);

        assertThat(pieces.get(dec(24))).containsExactly(
            solid(MORNING, true, false, VACATION_7, 0),
            bridge(NOON, false, false, VACATION_7, christmasEve, false)
        );
        assertThat(pieces.get(dec(27))).containsExactly(solid(FULL, false, false, VACATION_7, 2));
        assertThat(pieces.get(dec(30))).containsExactly(solid(FULL, false, false, VACATION_7, 0));
        assertThat(pieces.get(dec(31))).containsExactly(solid(FULL, false, true, VACATION_7, 0));
    }

    @Test
    void christmasEveOnASundayInsideAVacationKeepsBothGaps() {
        // December 2023: Fri 22nd, Sat 23rd, Sun 24th, holidays Mon 25th and Tue 26th
        final DateRange december2023 = new DateRange(LocalDate.of(2023, 12, 1), LocalDate.of(2023, 12, 31));
        final AbsenceBars.Gap christmasEve = new AbsenceBars.Gap("Heiligabend");
        final AbsenceBars.Gap christmas = new AbsenceBars.Gap("Weihnachten");

        final Map<LocalDate, List<AbsenceBars.Piece>> pieces = compute(Map.of(
            LocalDate.of(2023, 12, 22), full(VACATION_7),
            LocalDate.of(2023, 12, 23), gap(NO_WORKDAY),
            LocalDate.of(2023, 12, 24), new AbsenceBars.Day(new AbsenceBars.HalfDay(null, NO_WORKDAY), new AbsenceBars.HalfDay(null, christmasEve)),
            LocalDate.of(2023, 12, 25), gap(christmas),
            LocalDate.of(2023, 12, 26), gap(christmas),
            LocalDate.of(2023, 12, 27), full(VACATION_7)
        ), december2023);

        assertThat(pieces.get(LocalDate.of(2023, 12, 24))).containsExactly(
            bridge(MORNING, false, false, VACATION_7, NO_WORKDAY, false),
            bridge(NOON, false, false, VACATION_7, christmasEve, false)
        );
    }

    @Test
    void bridgeHalfCoveredByTheAbsenceIsNotMergedWithAnUncoveredOne() {
        final Map<LocalDate, List<AbsenceBars.Piece>> pieces = compute(Map.of(
            jan(10), full(SICK_7),
            jan(11), new AbsenceBars.Day(new AbsenceBars.HalfDay(SICK_7, NO_WORKDAY), new AbsenceBars.HalfDay(null, NO_WORKDAY)),
            jan(12), gap(NO_WORKDAY),
            jan(13), full(SICK_7)
        ), JANUARY_2025);

        assertThat(pieces.get(jan(11))).containsExactly(
            bridge(MORNING, false, false, SICK_7, NO_WORKDAY, true),
            bridge(NOON, false, false, SICK_7, NO_WORKDAY, false)
        );
    }

    @Test
    void absencesOnlyInTheMarginProduceNoPieces() {
        final Map<LocalDate, List<AbsenceBars.Piece>> pieces = compute(Map.of(
            dec(20), full(VACATION_7)
        ), JANUARY_2025);

        assertThat(pieces).isEmpty();
    }

    private static Map<LocalDate, List<AbsenceBars.Piece>> compute(Map<LocalDate, AbsenceBars.Day> days, DateRange visible) {
        final DateRange timeline = new DateRange(visible.startDate().minusDays(14), visible.endDate().plusDays(14));
        return AbsenceBars.compute(timeline, days, visible);
    }

    private static AbsenceBars.Absence absence(String key) {
        return new AbsenceBars.Absence(key, AbsenceBars.Status.ALLOWED, "ORANGE", "Erholungsurlaub", null, false);
    }

    private static AbsenceBars.Day full(AbsenceBars.Absence absence) {
        return new AbsenceBars.Day(new AbsenceBars.HalfDay(absence, null), new AbsenceBars.HalfDay(absence, null));
    }

    private static AbsenceBars.Day fullWithGap(AbsenceBars.Absence absence, AbsenceBars.Gap gap) {
        return new AbsenceBars.Day(new AbsenceBars.HalfDay(absence, gap), new AbsenceBars.HalfDay(absence, gap));
    }

    private static AbsenceBars.Day gap(AbsenceBars.Gap gap) {
        return new AbsenceBars.Day(new AbsenceBars.HalfDay(null, gap), new AbsenceBars.HalfDay(null, gap));
    }

    private static AbsenceBars.Piece solid(AbsenceBars.Half half, boolean roundedStart, boolean roundedEnd, AbsenceBars.Absence absence, int labelHalves) {
        return new AbsenceBars.Piece(half, SOLID, roundedStart, roundedEnd, absence, null, true, labelHalves);
    }

    private static AbsenceBars.Piece bridge(AbsenceBars.Half half, boolean roundedStart, boolean roundedEnd, AbsenceBars.Absence absence, AbsenceBars.Gap gap, boolean coveredByAbsence) {
        return new AbsenceBars.Piece(half, BRIDGE, roundedStart, roundedEnd, absence, gap, coveredByAbsence, 0);
    }

    private static LocalDate jan(int day) {
        return LocalDate.of(2025, 1, day);
    }

    private static LocalDate feb(int day) {
        return LocalDate.of(2025, 2, day);
    }

    private static LocalDate dec(int day) {
        return LocalDate.of(2024, 12, day);
    }
}
