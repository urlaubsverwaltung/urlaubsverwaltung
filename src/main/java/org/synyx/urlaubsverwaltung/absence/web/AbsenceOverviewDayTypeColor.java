package org.synyx.urlaubsverwaltung.absence.web;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;

public class AbsenceOverviewDayTypeColor {

    private final VacationTypeColor morning;
    private final VacationTypeColor noon;
    private final VacationTypeColor full;

    AbsenceOverviewDayTypeColor(VacationTypeColor morning, VacationTypeColor noon, VacationTypeColor full) {
        this.morning = morning;
        this.noon = noon;
        this.full = full;
    }

    public VacationTypeColor getMorning() {
        return morning;
    }

    public VacationTypeColor getNoon() {
        return noon;
    }

    public VacationTypeColor getFull() {
        return full;
    }
}
