package org.synyx.urlaubsverwaltung.workingtime;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.WeekDay;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WorkingTimeForm {

    private LocalDate validFrom;
    private List<Integer> workingDays = new ArrayList<>();
    private FederalState federalState;

    WorkingTimeForm() {
        // OK
    }

    WorkingTimeForm(WorkingTime workingTime) {

        for (WeekDay day : WeekDay.values()) {
            Integer dayOfWeek = day.getDayOfWeek();

            DayLength dayLength = workingTime.getDayLengthForWeekDay(dayOfWeek);

            if (dayLength != DayLength.ZERO) {
                workingDays.add(dayOfWeek);
            }
        }

        this.validFrom = workingTime.getValidFrom();
        this.federalState = workingTime.getFederalStateOverride().orElse(null);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkingTimeForm that = (WorkingTimeForm) o;
        return Objects.equals(validFrom, that.validFrom) &&
            Objects.equals(workingDays, that.workingDays) &&
            federalState == that.federalState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(validFrom, workingDays, federalState);
    }
}
