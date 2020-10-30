package org.synyx.urlaubsverwaltung.web;

import javax.swing.text.html.Option;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.util.DateUtil.getFirstDayOfYear;
import static org.synyx.urlaubsverwaltung.util.DateUtil.getLastDayOfYear;

/**
 * Represents a period of time to filter requests by.
 */
public class FilterPeriod {

    private LocalDate startDate;
    private LocalDate endDate;

    public FilterPeriod(LocalDate startDate, LocalDate endDate) {

        final int currentYear = Year.now(Clock.systemUTC()).getValue();

        this.startDate = startDate == null ? getFirstDayOfYear(currentYear) : startDate;
        this.endDate = endDate == null ? getLastDayOfYear(currentYear) : endDate;
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

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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
