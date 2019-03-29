package org.synyx.urlaubsverwaltung.workingtime.api;

class WorkDayResponse {

    private final String workDays;

    WorkDayResponse(String workDays) {

        this.workDays = workDays;
    }

    public String getWorkDays() {

        return workDays;
    }
}
