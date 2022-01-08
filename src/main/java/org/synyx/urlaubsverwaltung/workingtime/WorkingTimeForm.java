package org.synyx.urlaubsverwaltung.workingtime;

import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;

public class WorkingTimeForm {

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

        return validFrom.format(ISO_DATE);
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
