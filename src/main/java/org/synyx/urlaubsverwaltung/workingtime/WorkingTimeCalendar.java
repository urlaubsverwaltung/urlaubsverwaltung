package org.synyx.urlaubsverwaltung.workingtime;

import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * Provides information about the {@link DayLength} on a given {@link LocalDate} including publicHolidays.
 * For instance:
 * <ul>
 *     <li>2022-01-01 - DayLength.ZERO (publicHoliday, saturday)</li>
 *     <li>2022-01-02 - DayLength.ZERO (sunday)</li>
 *     <li>2022-01-03 - DayLength.FULL (monday)</li>
 *     <li>2022-01-04 - DayLength.FULL (tuesday)</li>
 *     <li>2022-01-05 - DayLength.ZERO (wednesday)</li>
 * </ul>
 *
 * Should be used in combination with a {@link Map} to keep relation to a {@link org.synyx.urlaubsverwaltung.person.Person} for example.
 */
public class WorkingTimeCalendar {

    private final Map<LocalDate, DayLength> workingTimeByDate;

    public WorkingTimeCalendar(Map<LocalDate, DayLength> workingTimeByDate) {
        this.workingTimeByDate = workingTimeByDate;
    }

    /**
     *
     * @param application
     * @return the dayLength workingTime for the given application date range. (e.g. 1.5 days)
     */
    public BigDecimal workingTime(Application application) {
        BigDecimal workingTimeSum = BigDecimal.ZERO;
        final DateRange applicationDateRange = new DateRange(application.getStartDate(), application.getEndDate());
        for (LocalDate localDate : applicationDateRange) {
            final BigDecimal workingTime = workingTime(localDate).orElse(BigDecimal.ZERO);
            if (application.getDayLength().isHalfDay()) {
                workingTimeSum = workingTimeSum.add(workingTime.divide(BigDecimal.valueOf(2),1, RoundingMode.CEILING));
            } else {
                workingTimeSum = workingTimeSum.add(workingTime);
            }
        }
        return workingTimeSum;
    }

    /**
     * Return the dayLength duration value for the given date.
     *
     * <p>
     *   If you are interested in the {@linkplain DayLength} to get information about morning or noon,
     *   see {@linkplain WorkingTimeCalendar#workingTimeDayLength(LocalDate)} instead.
     * </p>
     *
     * @param localDate
     * @return the dayLength workingTime for the given date (e.g. 0.5 days), or empty {@linkplain Optional} when there is no entry found.
     */
    public Optional<BigDecimal> workingTime(LocalDate localDate) {
        return workingTimeDayLength(localDate).map(DayLength::getDuration);
    }

    /**
     * Return the {@linkplain DayLength} for the given date.
     *
     * @param localDate
     * @return the {@linkplain DayLength} workingTime for the given date (e.g. DayLength.MORNING), or empty {@linkplain Optional} when there is no entry found.
     */
    public Optional<DayLength> workingTimeDayLength(LocalDate localDate) {
        if (workingTimeByDate.containsKey(localDate)) {
            return Optional.of(workingTimeByDate.get(localDate));
        }
        return Optional.empty();
    }

    /**
     *
     * @param from inclusive
     * @param to inclusive
     * @return the dayLength workingTime for the given date range. (e.g. 1.5 days)
     */
    public BigDecimal workingTime(LocalDate from, LocalDate to) {

        if (from.isAfter(to)) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = BigDecimal.ZERO;

        for (Map.Entry<LocalDate, DayLength> entry : workingTimeByDate.entrySet()) {
            final LocalDate localDate = entry.getKey();
            if (!localDate.isBefore(from) && !localDate.isAfter(to)) {
                final DayLength dayLength = entry.getValue();
                sum = sum.add(dayLength.getDuration());
            }
        }

        return sum;
    }
}
