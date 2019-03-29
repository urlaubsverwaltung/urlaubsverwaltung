package org.synyx.urlaubsverwaltung.workingtime.web;

import org.joda.time.DateMidnight;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.WeekDay;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;

import java.util.ArrayList;
import java.util.List;


class WorkingTimeForm {

    private DateMidnight validFrom;

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

    public DateMidnight getValidFrom() {

        return validFrom;
    }


    public void setValidFrom(DateMidnight validFrom) {

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
}
