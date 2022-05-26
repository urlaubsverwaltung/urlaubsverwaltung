package org.synyx.urlaubsverwaltung.absence.web;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;

public class AbsenceOverviewDayTypeColor {

    private final VacationTypeColor morning;
    private final VacationTypeColor noon;
    private final VacationTypeColor full;

    public static AbsenceOverviewDayTypeColor absenceOverviewDayTypeColorFull(VacationTypeColor fullColor) {
        return new AbsenceOverviewDayTypeColor(null, null, fullColor);
    }

    public static AbsenceOverviewDayTypeColor absenceOverviewDayTypeColorMorning(VacationTypeColor morningColor) {
        return new AbsenceOverviewDayTypeColor(morningColor, null, null);
    }

    public static AbsenceOverviewDayTypeColor absenceOverviewDayTypeColorNoon(VacationTypeColor noonColor) {
        return new AbsenceOverviewDayTypeColor(null, noonColor, null);
    }

    public static AbsenceOverviewDayTypeColor absenceOverviewDayTypeColorMorningAndNoon(VacationTypeColor morningColor, VacationTypeColor noonColor) {
        return new AbsenceOverviewDayTypeColor(morningColor, noonColor, null);
    }

    private AbsenceOverviewDayTypeColor(VacationTypeColor morning, VacationTypeColor noon, VacationTypeColor full) {
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
