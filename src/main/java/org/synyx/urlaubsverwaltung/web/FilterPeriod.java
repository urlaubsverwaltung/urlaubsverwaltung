package org.synyx.urlaubsverwaltung.web;

import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.util.DateFormat;
import org.synyx.urlaubsverwaltung.util.DateUtil;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;


/**
 * Represents a period of time to filter requests by.
 */
public class FilterPeriod {

    private LocalDate startDate;
    private LocalDate endDate;

    public FilterPeriod() {

        int currentYear = ZonedDateTime.now(UTC).getYear();

        this.startDate = DateUtil.getFirstDayOfYear(currentYear);
        this.endDate = DateUtil.getLastDayOfYear(currentYear);
    }


    public FilterPeriod(LocalDate startDate, LocalDate endDate) {

        Assert.notNull(startDate, "Start date must be given");
        Assert.notNull(endDate, "End date must be given");
        Assert.isTrue(endDate.isAfter(startDate) || endDate.isEqual(startDate), "Start date must be before end date");

        this.startDate = startDate;
        this.endDate = endDate;
    }


    public FilterPeriod(Optional<String> startDateAsString, Optional<String> endDateAsString) {

        Assert.notNull(startDateAsString, "Start date must be given");
        Assert.notNull(endDateAsString, "End date must be given");

        // Set default values for dates
        int currentYear = ZonedDateTime.now(UTC).getYear();
        this.startDate = DateUtil.getFirstDayOfYear(currentYear);
        this.endDate = DateUtil.getLastDayOfYear(currentYear);

        // Override default values with parsed dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateFormat.PATTERN);
        try {
            startDateAsString.ifPresent(startDateString -> this.startDate = LocalDate.parse(startDateString, formatter));
            endDateAsString.ifPresent(endDateString -> this.endDate = LocalDate.parse(endDateString, formatter));
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

        return getStartDate().format(DateTimeFormatter.ofPattern(DateFormat.PATTERN));
    }


    public String getEndDateAsString() {

        return getEndDate().format(DateTimeFormatter.ofPattern(DateFormat.PATTERN));
    }

    @Override
    public String toString() {
        return "FilterPeriod{" +
            "startDate=" + startDate +
            ", endDate=" + endDate +
            '}';
    }
}
