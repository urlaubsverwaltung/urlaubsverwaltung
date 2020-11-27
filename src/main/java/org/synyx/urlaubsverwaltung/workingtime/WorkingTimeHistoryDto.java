package org.synyx.urlaubsverwaltung.workingtime;

import java.time.LocalDate;
import java.util.List;

public final class WorkingTimeHistoryDto {

    private final LocalDate validFrom;
    private final List<String> workingDays;
    private final String federalState;
    private final boolean valid;

    WorkingTimeHistoryDto(LocalDate validFrom, List<String> workingDays, String federalState, boolean valid) {
        this.validFrom = validFrom;
        this.workingDays = workingDays;
        this.federalState = federalState;
        this.valid = valid;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public List<String> getWorkingDays() {
        return workingDays;
    }

    public String getFederalState() {
        return federalState;
    }

    public boolean isValid() {
        return valid;
    }
}
