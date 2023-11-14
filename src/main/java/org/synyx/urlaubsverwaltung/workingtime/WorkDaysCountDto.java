package org.synyx.urlaubsverwaltung.workingtime;

public class WorkDaysCountDto {

    private final String workDays;

    WorkDaysCountDto(String workDays) {
        this.workDays = workDays;
    }

    public String getWorkDays() {
        return workDays;
    }
}
