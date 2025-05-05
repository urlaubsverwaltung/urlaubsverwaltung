package org.synyx.urlaubsverwaltung.absence.web;

public class AbsenceOverviewDayTypeDescription {

    private final String morning;
    private final String noon;
    private final String full;

    AbsenceOverviewDayTypeDescription(String morning, String noon, String full) {
        this.morning = morning;
        this.noon = noon;
        this.full = full;
    }

    public String getMorning() {
        return morning;
    }

    public String getNoon() {
        return noon;
    }

    public String getFull() {
        return full;
    }
}
