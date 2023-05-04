package org.synyx.urlaubsverwaltung.workingtime;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.ISO_DATE;

public final class WorkingTimeHistoryDto {

    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private final LocalDate validFrom;
    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private final LocalDate validTo;
    private final List<String> workingDays;
    private final String country;
    private final String federalState;
    private final boolean valid;

    WorkingTimeHistoryDto(LocalDate validFrom, LocalDate validTo, List<String> workingDays, String country, String federalState, boolean valid) {
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.workingDays = workingDays;
        this.country = country;
        this.federalState = federalState;
        this.valid = valid;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public List<String> getWorkingDays() {
        return workingDays;
    }

    public String getCountry() {
        return country;
    }

    public String getFederalState() {
        return federalState;
    }

    public boolean isValid() {
        return valid;
    }
}
