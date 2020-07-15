package org.synyx.urlaubsverwaltung.web;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.synyx.urlaubsverwaltung.util.DateFormat;
import org.synyx.urlaubsverwaltung.util.DateUtil;

import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


/**
 * Represents a period of time to filter requests by.
 */
public class FilterPeriod {

    private Instant startDate;
    private Instant endDate;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateFormat.PATTERN);

    public FilterPeriod(String startDateAsString, String endDateAsString) {

        int currentYear = Year.now(Clock.systemUTC()).getValue();
        try {
            this.startDate = StringUtils.isEmpty(startDateAsString) ?
                DateUtil.getFirstDayOfYear(currentYear) : Instant.from(formatter.parse(startDateAsString));
            this.endDate = StringUtils.isEmpty(endDateAsString) ?
                DateUtil.getLastDayOfYear(currentYear) : Instant.from(formatter.parse(endDateAsString));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(exception.getMessage());
        }

        Assert.isTrue(endDate.isAfter(startDate) || endDate.equals(startDate), "Start date must be before end date");
    }

    public Instant getStartDate() {

        return startDate;
    }


    public Instant getEndDate() {

        return endDate;
    }


    public void setStartDate(Instant startDate) {

        this.startDate = startDate;
    }


    public void setEndDate(Instant endDate) {

        this.endDate = endDate;
    }


    public String getStartDateAsString() {

        return formatter.format(getStartDate());
    }


    public String getEndDateAsString() {

        return formatter.format(getEndDate());
    }

    @Override
    public String toString() {
        return "FilterPeriod{" +
            "startDate=" + startDate +
            ", endDate=" + endDate +
            '}';
    }
}
