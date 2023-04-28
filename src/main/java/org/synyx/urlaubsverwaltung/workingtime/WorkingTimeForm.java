package org.synyx.urlaubsverwaltung.workingtime;

import org.springframework.format.annotation.DateTimeFormat;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.D_M_YYYY;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.ISO_DATE;

public class WorkingTimeForm {

    @DateTimeFormat(pattern = DD_MM_YYYY, fallbackPatterns = {D_M_YY, D_M_YYYY, ISO_DATE})
    private LocalDate validFrom;
    private List<Integer> workingDays = new ArrayList<>();
    private FederalState federalState;
    private boolean isDefaultFederalState;

    WorkingTimeForm() {
        // OK
    }

    WorkingTimeForm(WorkingTime workingTime) {

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            final DayLength dayLength = workingTime.getDayLengthForWeekDay(dayOfWeek);
            if (dayLength != ZERO) {
                workingDays.add(dayOfWeek.getValue());
            }
        }

        this.validFrom = workingTime.getValidFrom();
        this.federalState = workingTime.getFederalState();
        this.isDefaultFederalState = workingTime.isDefaultFederalState();
    }

    public String getValidFromIsoValue() {
        if (validFrom == null) {
            return "";
        }

        return validFrom.format(DateTimeFormatter.ISO_DATE);
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public List<Integer> getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(List<Integer> workingDays) {
        this.workingDays = workingDays;
    }

    public FederalState getFederalState() {
        return federalState;
    }

    public void setFederalState(FederalState federalState) {
        this.federalState = federalState;
    }

    public boolean isDefaultFederalState() {
        return isDefaultFederalState;
    }

    public void setDefaultFederalState(boolean defaultFederalState) {
        isDefaultFederalState = defaultFederalState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkingTimeForm that = (WorkingTimeForm) o;
        return isDefaultFederalState == that.isDefaultFederalState
            && Objects.equals(validFrom, that.validFrom)
            && Objects.equals(workingDays, that.workingDays)
            && federalState == that.federalState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(validFrom, workingDays, federalState, isDefaultFederalState);
    }
}
