package org.synyx.urlaubsverwaltung.absence;

import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Represents a immutable date range
 * <p>
 * A date range represents a period of time between two LocalDates.
 * Date range are inclusive of the start and the end date.
 * The end date is always greater than or equal to the start date.
 * <p>
 */
public final class DateRange implements Iterable<LocalDate> {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public DateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            Assert.isTrue(!startDate.isAfter(endDate), "The end date must be greater than or equal to the start date.");
        }

        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
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


    @Override
    public Iterator<LocalDate> iterator() {
        return new DateRangeIterator(startDate, endDate);
    }

    public Stream<LocalDate> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    private static final class DateRangeIterator implements Iterator<LocalDate> {

        private LocalDate cursor;
        private final LocalDate endDate;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateRange that = (DateRange) o;
        return Objects.equals(startDate, that.startDate) && Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate);
    }
}
