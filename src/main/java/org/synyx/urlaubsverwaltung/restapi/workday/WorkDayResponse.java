package org.synyx.urlaubsverwaltung.restapi.workday;

class WorkDayResponse {

    private final String workDays;

    WorkDayResponse(String workDays) {

        this.workDays = workDays;
    }

    public String getWorkDays() {

        return workDays;
    }
}
