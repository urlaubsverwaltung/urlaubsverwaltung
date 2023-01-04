package org.synyx.urlaubsverwaltung.web;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a period of time to filter requests by.
 */
public final class FilterPeriod {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public FilterPeriod(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public String getStartDateIsoValue() {
        return Optional.ofNullable(startDate)
            .map(date -> date.format(DateTimeFormatter.ISO_DATE))
            .orElse("");
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getEndDateIsoValue() {
        return Optional.ofNullable(endDate)
            .map(date -> date.format(DateTimeFormatter.ISO_DATE))
            .orElse("");
    }

    @Override
    public String toString() {
        return "FilterPeriod{" +
            "startDate=" + startDate +
            ", endDate=" + endDate +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final FilterPeriod period = (FilterPeriod) o;

        return Objects.equals(getStartDate(), period.getStartDate()) &&
            Objects.equals(getEndDate(), period.getEndDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStartDate(), getEndDate());
    }
}
