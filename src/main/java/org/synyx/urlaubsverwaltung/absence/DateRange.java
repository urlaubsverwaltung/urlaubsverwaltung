package org.synyx.urlaubsverwaltung.absence;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class DateRange implements Iterable<LocalDate> {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public DateRange(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
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
