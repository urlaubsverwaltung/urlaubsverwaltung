package org.synyx.urlaubsverwaltung.web;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;

import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.springframework.util.StringUtils.isEmpty;
import static org.synyx.urlaubsverwaltung.util.DateFormat.DD_MM_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateUtil.getFirstDayOfYear;
import static org.synyx.urlaubsverwaltung.util.DateUtil.getLastDayOfYear;

/**
 * Represents a period of time to filter requests by.
 */
public class FilterPeriod {

    private LocalDate startDate;
    private LocalDate endDate;

    public FilterPeriod(String startDateAsString, String endDateAsString) {

        int currentYear = Year.now(Clock.systemUTC()).getValue();
        try {
            startDate = isEmpty(startDateAsString) ? getFirstDayOfYear(currentYear) : parse(startDateAsString, ofPattern(DD_MM_YYYY));
            endDate = isEmpty(endDateAsString) ? getLastDayOfYear(currentYear) : parse(endDateAsString, ofPattern(DD_MM_YYYY));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(exception.getMessage());
        }
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

    public String getStartDateAsString() {
        return Optional.ofNullable(getStartDate())
            .map(localDate -> localDate.format(ofPattern(DD_MM_YYYY)))
            .orElse("");
    }

    public String getEndDateAsString() {
        return Optional.ofNullable(getEndDate())
            .map(localDate -> localDate.format(ofPattern(DD_MM_YYYY)))
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
