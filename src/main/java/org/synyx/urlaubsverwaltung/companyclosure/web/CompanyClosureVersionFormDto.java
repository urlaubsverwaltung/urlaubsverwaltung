package org.synyx.urlaubsverwaltung.companyclosure.web;

import org.springframework.format.annotation.DateTimeFormat;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.ISO_DATE;

/**
 * A version of a company closure that has not started yet, so it can still be changed. It is identified by the date
 * it started from when the form was shown, since its new "valid from" may differ.
 */
public class CompanyClosureVersionFormDto implements Serializable {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate originalValidFrom;
    private DayLength dayLength;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate validFrom;

    public CompanyClosureVersionFormDto() {
        // used by data binding
    }

    public CompanyClosureVersionFormDto(DayLength dayLength, LocalDate validFrom) {
        this.originalValidFrom = validFrom;
        this.dayLength = dayLength;
        this.validFrom = validFrom;
    }

    public LocalDate getOriginalValidFrom() {
        return originalValidFrom;
    }

    public void setOriginalValidFrom(LocalDate originalValidFrom) {
        this.originalValidFrom = originalValidFrom;
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

    /**
     * @return whether day length or "valid from" differ from the version as it was shown
     */
    public boolean isChanged(DayLength originalDayLength) {
        return dayLength != originalDayLength || validFrom == null || !validFrom.equals(originalValidFrom);
    }
}
