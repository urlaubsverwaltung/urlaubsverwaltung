package org.synyx.urlaubsverwaltung.workingtime.api;

class WorkDayDto {

    private final String workDays;

    WorkDayDto(String workDays) {
        this.workDays = workDays;
    }

    public String getWorkDays() {
        return workDays;
    }
}
