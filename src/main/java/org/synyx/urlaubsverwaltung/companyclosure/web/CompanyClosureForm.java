package org.synyx.urlaubsverwaltung.companyclosure.web;

import org.springframework.format.annotation.DateTimeFormat;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.ISO_DATE;

/**
 * Day and month are only used to create a closure day, an existing one is identified by the url.
 */
public class CompanyClosureForm {

    private Integer day;
    private Integer month;
    private String name;
    private DayLength dayLength;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate validFrom;

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DayLength getDayLength() {
        return dayLength;
    }

    public void setDayLength(DayLength dayLength) {
        this.dayLength = dayLength;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public String getValidFromIsoValue() {
        return validFrom == null ? "" : validFrom.format(DateTimeFormatter.ISO_DATE);
    }
}
