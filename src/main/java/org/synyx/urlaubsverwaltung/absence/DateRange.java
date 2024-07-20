package org.synyx.urlaubsverwaltung.absence;

import org.springframework.util.Assert;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Represents an immutable date range
 * <p>
 * A date range represents a period of time between two LocalDates.
 * Date range are inclusive of the start and the end date.
 * The end date is always greater than or equal to the start date.
 *
 * <p>
 * So a duration between startDate and endDate is two days if endDate is the next day.
 *
 * @param startDate start of the date range, inclusive
 * @param endDate end of the date range, inclusive
 */
public record DateRange(LocalDate startDate, LocalDate endDate) implements Iterable<LocalDate> {

    public DateRange {
        if (startDate != null && endDate != null) {
            Assert.isTrue(!startDate.isAfter(endDate), "The end date must be greater than or equal to the start date.");
        }
    }

    /**
     * Checks if this date ranges overlap each other.
     *
     * @param dateRange to check for overlap
     * @return true if the date ranges overlap, false otherwise
     */
    public boolean isOverlapping(final DateRange dateRange) {
        return (startDate.isBefore(dateRange.endDate) || startDate.isEqual(dateRange.endDate)) &&
            (dateRange.startDate.isBefore(endDate) || dateRange.startDate.isEqual(endDate));
    }

    /**
     * Returns the overlapping date range of given date ranges.
     *
     * <p>
     * Date ranges are inclusive of the start and the end date.
     * A date range overlaps another if it shares some common part of the
     * dates. This method returns the amount of the overlap,
     * only if the date ranges actually do overlap.
     * If the date ranges do not overlap, an empty {@link DateRange} is returned.
     * <p>
     * When two date ranges are compared, the result is one of three states:
     * (a) they abut
     * (b) there is a gap between them
     * (c) they overlap.
     * The abuts state takes precedence over the other two, thus a zero duration
     * date range at the start of a larger date range abuts and does not overlap.
     *
     * @param dateRange overlapping date range
     * @return the overlapping date rage if given, else empty optional
     */
    public Optional<DateRange> overlap(final DateRange dateRange) {
        if (!isOverlapping(dateRange)) {
            return Optional.empty();
        }

        final LocalDate overlapStartDate = startDate.isBefore(dateRange.startDate) ? dateRange.startDate : startDate;
        final LocalDate overlapEndDate = endDate.isBefore(dateRange.endDate) ? endDate : dateRange.endDate;

        return Optional.of(new DateRange(overlapStartDate, overlapEndDate));
    }

    /**
     * Gets the gap between this date range and another date range.
     * The other date range can be either before or after this date range.
     * <p>
     * Date ranges are inclusive of the start and the end.
     * A date range has a gap to another date range if there are non-zero
     * dates between them. This method returns the amount of the gap only
     * if the date range do actually have a gap between them.
     * If the date range overlap or abut, then null is returned.
     * <p>
     * When two date ranges are compared the result is one of three states:
     * (a) they abut
     * (b) there is a gap between them
     * (c) they overlap.
     * The abuts state takes precedence over the other two, thus a zero duration
     * date range at the start of a larger date range abuts and does not overlap.
     *
     * @param dateRange date range containing the gap
     * @return the date rage with the gap if given, else empty optional
     */
    public Optional<DateRange> gap(final DateRange dateRange) {
        if (isOverlapping(dateRange)) {
            return Optional.empty();
        }

        Optional<DateRange> maybeDateRange = Optional.empty();
        if (startDate.isAfter(dateRange.endDate) && dateRange.endDate.until(startDate, DAYS) > 1) {
            maybeDateRange = Optional.of(new DateRange(dateRange.endDate.plusDays(1), startDate.minusDays(1)));
        } else if (dateRange.startDate.isAfter(endDate) && endDate.until(dateRange.startDate, DAYS) > 1) {
            maybeDateRange = Optional.of(new DateRange(endDate.plusDays(1), dateRange.startDate.minusDays(1)));
        }

        return maybeDateRange;
    }

    /**
     * An date range is empty if the start date or the end date are not represented
     *
     * @return true if empty date range, false otherwise
     */
    public boolean isEmpty() {
        return startDate == null || endDate == null;
    }

    /**
     * Calculates the days between the start and the end date (inclusive start and end date)
     * <p>
     * examples:
     * <ul>
     *     <li>2022-10-10 until 2022-10-10 is one day</li>
     *     <li>2022-10-10 until 2022-10-11 is two days</li>
     *     <li>2022-10-10 until 2022-10-20 are eleven days</li>
     * </ul>
     *
     * @return duration of days between start and end date (inclusive start and end date)
     */
    public Duration duration() {

        if (this.isEmpty()) {
            return Duration.ZERO;
        }

        return Duration.ofDays(startDate.until(endDate, DAYS)).plusDays(1);
    }

    @Override
    public Iterator<LocalDate> iterator() {
        return new DateRangeIterator(startDate, endDate);
    }

    public Stream<LocalDate> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    private static final class DateRangeIterator implements Iterator<LocalDate> {

        private final LocalDate endDate;
        private LocalDate cursor;

        DateRangeIterator(LocalDate startDate, LocalDate endDate) {
            this.cursor = startDate;
            this.endDate = endDate;
        }

        @Override
        public boolean hasNext() {
            return cursor.isBefore(endDate) || cursor.isEqual(endDate);
        }

        @Override
        public LocalDate next() {

            if (cursor.isAfter(endDate)) {
                throw new NoSuchElementException("next date is after endDate which is not in range anymore.");
            }

            final LocalDate current = cursor;
            cursor = cursor.plusDays(1);
            return current;
        }
    }
}
