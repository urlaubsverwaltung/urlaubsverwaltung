package org.synyx.urlaubsverwaltung.restapi;

/**
 * @author  David Schilling - schilling@synyx.de
 */
class WorkDayResponse {

    private final String workDays;

    WorkDayResponse(String workDays) {

        this.workDays = workDays;
    }

    public String getWorkDays() {

        return workDays;
    }
}
