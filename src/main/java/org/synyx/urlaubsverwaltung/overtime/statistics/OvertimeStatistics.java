package org.synyx.urlaubsverwaltung.overtime.statistics;

import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.util.DecimalConverter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;

/**
 * A company-wide statistic about overtime of all persons of a year.
 */
public class OvertimeStatistics {

    private static final BigDecimal SECONDS_PER_HOUR = BigDecimal.valueOf(3600);

    private final int year;
    private final LocalDate referenceDate;
    private final List<BigDecimal> totalOvertimeHoursByMonth;
    private final List<BigDecimal> deltaToPreviousMonthHours;
    private final List<DeltaTrend> deltaTrend;
    private final BigDecimal currentMonthTotalHours;
    private final BigDecimal deltaToPreviousMonthPercent;
    private final BigDecimal rolling30DayAveragePerPerson;

    OvertimeStatistics(Year year, LocalDate referenceDate, List<Overtime> overtimes, int numberOfPersons) {

        this.year = year.getValue();
        this.referenceDate = referenceDate;

        final int referenceMonthIndex = referenceDate.getMonthValue() - 1;

        this.totalOvertimeHoursByMonth = calculateTotalOvertimeHoursByMonth(year, overtimes);
        this.deltaToPreviousMonthHours = calculateDeltaToPreviousMonthHours(totalOvertimeHoursByMonth);
        this.deltaTrend = calculateDeltaTrend(deltaToPreviousMonthHours, referenceMonthIndex);

        this.currentMonthTotalHours = totalOvertimeHoursByMonth.get(referenceMonthIndex);
        this.deltaToPreviousMonthPercent = calculateDeltaToPreviousMonthPercent(totalOvertimeHoursByMonth, referenceMonthIndex);
        this.rolling30DayAveragePerPerson = calculateRolling30DayAveragePerPerson(year, referenceDate, overtimes, numberOfPersons);
    }

    public int getYear() {
        return year;
    }

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public List<BigDecimal> getTotalOvertimeHoursByMonth() {
        return totalOvertimeHoursByMonth;
    }

    public List<BigDecimal> getDeltaToPreviousMonthHours() {
        return deltaToPreviousMonthHours;
    }

    public List<DeltaTrend> getDeltaTrend() {
        return deltaTrend;
    }

    public BigDecimal getCurrentMonthTotalHours() {
        return currentMonthTotalHours;
    }

    public BigDecimal getDeltaToPreviousMonthPercent() {
        return deltaToPreviousMonthPercent;
    }

    public BigDecimal getRolling30DayAveragePerPerson() {
        return rolling30DayAveragePerPerson;
    }

    private static List<BigDecimal> calculateTotalOvertimeHoursByMonth(Year year, List<Overtime> overtimes) {

        final List<BigDecimal> values = new ArrayList<>();

        for (final Month month : Month.values()) {

            final LocalDate firstDateOfMonth = year.atMonth(month).atDay(1);
            final LocalDate lastDateOfMonth = year.atMonth(month).atEndOfMonth();
            final DateRange monthDateRange = new DateRange(firstDateOfMonth, lastDateOfMonth);

            values.add(toHours(sumDurationForDateRange(overtimes, monthDateRange)));
        }

        return values;
    }

    private static List<BigDecimal> calculateDeltaToPreviousMonthHours(List<BigDecimal> totalOvertimeHoursByMonth) {

        final List<BigDecimal> deltas = new ArrayList<>();
        deltas.add(null);

        for (int i = 1; i < totalOvertimeHoursByMonth.size(); i++) {
            deltas.add(totalOvertimeHoursByMonth.get(i).subtract(totalOvertimeHoursByMonth.get(i - 1)));
        }

        return deltas;
    }

    private static List<DeltaTrend> calculateDeltaTrend(List<BigDecimal> deltaToPreviousMonthHours, int referenceMonthIndex) {

        final List<DeltaTrend> trends = new ArrayList<>();

        for (int i = 0; i < deltaToPreviousMonthHours.size(); i++) {

            final BigDecimal delta = deltaToPreviousMonthHours.get(i);

            if (delta == null) {
                trends.add(DeltaTrend.NONE);
            } else if (i == referenceMonthIndex) {
                trends.add(DeltaTrend.NEUTRAL);
            } else if (delta.signum() < 0) {
                trends.add(DeltaTrend.DECREASE);
            } else if (delta.signum() > 0) {
                trends.add(DeltaTrend.INCREASE);
            } else {
                trends.add(DeltaTrend.NEUTRAL);
            }
        }

        return trends;
    }

    private static BigDecimal calculateDeltaToPreviousMonthPercent(List<BigDecimal> totalOvertimeHoursByMonth, int referenceMonthIndex) {

        if (referenceMonthIndex == 0) {
            // January: no previous-year data has been fetched, delta is not available.
            return null;
        }

        final BigDecimal previousMonthTotal = totalOvertimeHoursByMonth.get(referenceMonthIndex - 1);
        if (previousMonthTotal.compareTo(ZERO) == 0) {
            return null;
        }

        final BigDecimal currentMonthTotal = totalOvertimeHoursByMonth.get(referenceMonthIndex);

        return currentMonthTotal.subtract(previousMonthTotal)
            .divide(previousMonthTotal, 4, HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(2, HALF_UP);
    }

    private static BigDecimal calculateRolling30DayAveragePerPerson(Year year, LocalDate referenceDate, List<Overtime> overtimes, int numberOfPersons) {

        if (numberOfPersons == 0) {
            return ZERO;
        }

        final LocalDate firstDayOfYear = year.atDay(1);
        final LocalDate earliestWindowStart = referenceDate.minusDays(29);
        // clamped to the selected year: no cross-year fetch has been done, so the window is shortened
        // near the start of a year rather than silently including unfetched data.
        final LocalDate windowStart = earliestWindowStart.isBefore(firstDayOfYear) ? firstDayOfYear : earliestWindowStart;
        final DateRange window = new DateRange(windowStart, referenceDate);

        final BigDecimal totalHours = toHours(sumDurationForDateRange(overtimes, window));

        return totalHours.divide(BigDecimal.valueOf(numberOfPersons), 2, HALF_UP);
    }

    private static Duration sumDurationForDateRange(List<Overtime> overtimes, DateRange dateRange) {
        return overtimes.stream()
            .filter(overtime -> overtime.dateRange().isOverlapping(dateRange))
            .map(overtime -> overtime.durationForDateRange(dateRange))
            .reduce(Duration.ZERO, Duration::plus);
    }

    private static BigDecimal toHours(Duration duration) {
        // DecimalConverter#toFormattedDecimal returns seconds, not hours, despite the name.
        return DecimalConverter.toFormattedDecimal(duration).divide(SECONDS_PER_HOUR, 2, HALF_UP);
    }

    /**
     * Visual trend of a month's overtime delta compared to the previous month.
     */
    public enum DeltaTrend {
        /** overtime decreased compared to the previous month. */
        DECREASE("overtime-delta--decrease"),
        /** overtime increased compared to the previous month. */
        INCREASE("overtime-delta--increase"),
        /** no change, or this is the reference ("current") month which is always shown neutral. */
        NEUTRAL("overtime-delta--neutral"),
        /** no previous month data available for comparison (January). */
        NONE("overtime-delta--none");

        private final String cssClass;

        DeltaTrend(String cssClass) {
            this.cssClass = cssClass;
        }

        public String getCssClass() {
            return cssClass;
        }
    }
}
