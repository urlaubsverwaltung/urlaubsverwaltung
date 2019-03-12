package org.synyx.urlaubsverwaltung.web;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.core.util.DateFormat;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;

import java.util.Optional;


/**
 * Represents a period of time to filter requests by.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class FilterPeriod {

    private DateMidnight startDate;
    private DateMidnight endDate;

    public FilterPeriod() {

        int currentYear = DateMidnight.now().getYear();

        this.startDate = DateUtil.getFirstDayOfYear(currentYear);
        this.endDate = DateUtil.getLastDayOfYear(currentYear);
    }


    public FilterPeriod(DateMidnight startDate, DateMidnight endDate) {

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
        int currentYear = DateMidnight.now().getYear();
        this.startDate = DateUtil.getFirstDayOfYear(currentYear);
        this.endDate = DateUtil.getLastDayOfYear(currentYear);

        // Override default values with parsed dates
        DateTimeFormatter formatter = DateTimeFormat.forPattern(DateFormat.PATTERN);

        startDateAsString.ifPresent(startDateString -> this.startDate = DateMidnight.parse(startDateString, formatter));
        endDateAsString.ifPresent(endDateString -> this.endDate = DateMidnight.parse(endDateString, formatter));

        Assert.isTrue(endDate.isAfter(startDate) || endDate.isEqual(startDate), "Start date must be before end date");
    }

    public DateMidnight getStartDate() {

        return startDate;
    }


    public DateMidnight getEndDate() {

        return endDate;
    }


    public void setStartDate(DateMidnight startDate) {

        this.startDate = startDate;
    }


    public void setEndDate(DateMidnight endDate) {

        this.endDate = endDate;
    }


    public String getStartDateAsString() {

        return getStartDate().toString(DateFormat.PATTERN);
    }


    public String getEndDateAsString() {

        return getEndDate().toString(DateFormat.PATTERN);
    }
}
