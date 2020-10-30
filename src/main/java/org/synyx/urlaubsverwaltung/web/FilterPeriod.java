package org.synyx.urlaubsverwaltung.web;

import org.springframework.util.Assert;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeParseException;

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

        Assert.isTrue(endDate.isAfter(startDate) || endDate.isEqual(startDate), "Start date must be before end date");
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStartDateAsString() {
        return getStartDate().format(ofPattern(DD_MM_YYYY));
    }

    public String getEndDateAsString() {
        return getEndDate().format(ofPattern(DD_MM_YYYY));
    }

    @Override
    public String toString() {
        return "FilterPeriod{" +
            "startDate=" + startDate +
            ", endDate=" + endDate +
            '}';
    }
}
