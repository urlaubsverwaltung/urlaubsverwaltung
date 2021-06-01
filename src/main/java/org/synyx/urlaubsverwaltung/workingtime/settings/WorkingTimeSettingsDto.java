package org.synyx.urlaubsverwaltung.workingtime.settings;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;

public class WorkingTimeSettingsDto {

    private Integer id;
    private DayLength monday;
    private DayLength tuesday;
    private DayLength wednesday;
    private DayLength thursday;
    private DayLength friday;
    private DayLength saturday;
    private DayLength sunday;
    private DayLength workingDurationForChristmasEve;
    private DayLength workingDurationForNewYearsEve;
    private FederalState federalState;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DayLength getMonday() {
        return monday;
    }

    public void setMonday(DayLength monday) {
        this.monday = monday;
    }

    public DayLength getTuesday() {
        return tuesday;
    }

    public void setTuesday(DayLength tuesday) {
        this.tuesday = tuesday;
    }

    public DayLength getWednesday() {
        return wednesday;
    }

    public void setWednesday(DayLength wednesday) {
        this.wednesday = wednesday;
    }

    public DayLength getThursday() {
        return thursday;
    }

    public void setThursday(DayLength thursday) {
        this.thursday = thursday;
    }

    public DayLength getFriday() {
        return friday;
    }

    public void setFriday(DayLength friday) {
        this.friday = friday;
    }

    public DayLength getSaturday() {
        return saturday;
    }

    public void setSaturday(DayLength saturday) {
        this.saturday = saturday;
    }

    public DayLength getSunday() {
        return sunday;
    }

    public void setSunday(DayLength sunday) {
        this.sunday = sunday;
    }

    public DayLength getWorkingDurationForChristmasEve() {
        return workingDurationForChristmasEve;
    }

    public void setWorkingDurationForChristmasEve(DayLength workingDurationForChristmasEve) {
        this.workingDurationForChristmasEve = workingDurationForChristmasEve;
    }

    public DayLength getWorkingDurationForNewYearsEve() {
        return workingDurationForNewYearsEve;
    }

    public void setWorkingDurationForNewYearsEve(DayLength workingDurationForNewYearsEve) {
        this.workingDurationForNewYearsEve = workingDurationForNewYearsEve;
    }

    public FederalState getFederalState() {
        return federalState;
    }

    public void setFederalState(FederalState federalState) {
        this.federalState = federalState;
    }

    public void setWorkingDays(List<Integer> workingDays) {

        setAllDayLengthsToZero();

        if (!workingDays.isEmpty()) {
            for (Integer dayOfWeek : workingDays) {
                switch (DayOfWeek.of(dayOfWeek)) {
                    case MONDAY:
                        this.monday = FULL;
                        break;
                    case TUESDAY:
                        this.tuesday = FULL;
                        break;
                    case WEDNESDAY:
                        this.wednesday = FULL;
                        break;
                    case THURSDAY:
                        this.thursday = FULL;
                        break;
                    case FRIDAY:
                        this.friday = FULL;
                        break;
                    case SATURDAY:
                        this.saturday = FULL;
                        break;
                    case SUNDAY:
                        this.sunday = FULL;
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public List<Integer> getWorkingDays() {
        final List<Integer> workingDays = new ArrayList<>();

        if (monday.equals(FULL)) {
            workingDays.add(1);
        }
        if (tuesday.equals(FULL)) {
            workingDays.add(2);
        }
        if (wednesday.equals(FULL)) {
            workingDays.add(3);
        }
        if (thursday.equals(FULL)) {
            workingDays.add(4);
        }
        if (friday.equals(FULL)) {
            workingDays.add(5);
        }
        if (saturday.equals(FULL)) {
            workingDays.add(6);
        }
        if (sunday.equals(FULL)) {
            workingDays.add(7);
        }

        return workingDays;
    }

    private void setAllDayLengthsToZero() {
        this.monday = ZERO;
        this.tuesday = ZERO;
        this.wednesday = ZERO;
        this.thursday = ZERO;
        this.friday = ZERO;
        this.saturday = ZERO;
        this.sunday = ZERO;
    }
}
