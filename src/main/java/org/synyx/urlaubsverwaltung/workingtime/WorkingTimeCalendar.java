package org.synyx.urlaubsverwaltung.workingtime;

import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.PUBLIC_HOLIDAY;

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
 * <p>
 * Should be used in combination with a {@link Map} to keep relation to a {@link org.synyx.urlaubsverwaltung.person.Person} for example.
 */
public record WorkingTimeCalendar(Map<LocalDate, WorkingDayInformation> workingDays) {

    /**
     * Calculates the next date of a working day.
     *
     * <p>
     * Examples given working days are MONDAY, TUESDAY and FRIDAY
     * <ul>
     *     <li>{@code monday} -> tuesday</li>
     *     <li>{@code tuesday} -> friday</li>
     *     <li>{@code wednesday} -> friday</li>
     *     <li>{@code thursday} -> friday</li>
     *     <li>{@code friday} -> monday</li>
     * </ul>
     *
     * @param localDate any local date
     * @return optional resolving to a {@linkplain LocalDate} of the next working day when it exists in this calendar.
     */
    public Optional<LocalDate> nextWorkingFollowingTo(LocalDate localDate) {

        final LocalDate nextDay = localDate.plusDays(1);
        final Optional<DayLength> dayLength = workingTimeDayLength(nextDay);

        if (dayLength.isEmpty()) {
            return Optional.empty();
        } else if (dayLength.get() != DayLength.ZERO) {
            return Optional.of(nextDay);
        } else {
            return nextWorkingFollowingTo(nextDay);
        }
    }

    /**
     * @param application
     * @return the dayLength workingTime for the given application date range. (e.g. 1.5 days)
     */
    public BigDecimal workingTime(Application application) {
        return workingTimeInDateRage(application, new DateRange(application.getStartDate(), application.getEndDate()));
    }

    /**
     * @param application which is the base for the calculation of working days to be absent
     * @param dateRange   are the days which are from interest
     * @return the dayLength workingTime for the given application and date range. (e.g. 1.5 days)
     */
    public BigDecimal workingTimeInDateRage(Application application, DateRange dateRange) {
        final DateRange applicationDateRange = new DateRange(application.getStartDate(), application.getEndDate());
        final Optional<DateRange> overlap = dateRange.overlap(applicationDateRange);
        if (overlap.isEmpty()) {
            return BigDecimal.ZERO;
        } else {
            BigDecimal workingTimeSum = BigDecimal.ZERO;
            for (LocalDate localDate : overlap.get()) {
                final BigDecimal workingTime = workingTime(localDate).orElse(BigDecimal.ZERO);
                final WorkingDayInformation workingDayInformation = this.workingDays().get(application.getStartDate());
                if (application.getDayLength().isHalfDay() && workingDayInformation != null && !workingDayInformation.hasHalfDayPublicHoliday()) {
                    workingTimeSum = workingTimeSum.add(workingTime.divide(BigDecimal.valueOf(2), 1, RoundingMode.CEILING));
                } else {
                    workingTimeSum = workingTimeSum.add(workingTime);
                }
            }
            return workingTimeSum;
        }
    }

    /**
     * Return the dayLength duration value for the given date.
     *
     * <p>
     * If you are interested in the {@linkplain DayLength} to get information about morning or noon,
     * see {@linkplain WorkingTimeCalendar#workingTimeDayLength(LocalDate)} instead.
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
        if (workingDays.containsKey(localDate)) {
            return Optional.of(workingDays.get(localDate).dayLength());
        }
        return Optional.empty();
    }

    /**
     * @param from inclusive
     * @param to   inclusive
     * @return the dayLength workingTime for the given date range. (e.g. 1.5 days)
     */
    public BigDecimal workingTime(LocalDate from, LocalDate to) {

        if (from.isAfter(to)) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = BigDecimal.ZERO;

        for (Map.Entry<LocalDate, WorkingDayInformation> entry : workingDays.entrySet()) {
            final LocalDate localDate = entry.getKey();
            if (!localDate.isBefore(from) && !localDate.isAfter(to)) {
                final DayLength dayLength = entry.getValue().dayLength();
                sum = sum.add(dayLength.getDuration());
            }
        }

        return sum;
    }

    public record WorkingDayInformation(
        DayLength dayLength,
        WorkingDayInformation.WorkingTimeCalendarEntryType morning,
        WorkingDayInformation.WorkingTimeCalendarEntryType noon
    ) {
        public enum WorkingTimeCalendarEntryType {
            WORKDAY,
            NO_WORKDAY,
            PUBLIC_HOLIDAY
        }

        public boolean hasHalfDayPublicHoliday() {
            return morning == PUBLIC_HOLIDAY || noon == PUBLIC_HOLIDAY;
        }
    }
}
