package org.synyx.urlaubsverwaltung.absence.web;

public enum AbsenceOverviewDayType {

    ABSENCE_FULL("absenceFull"),
    ABSENCE_MORNING("absenceMorning"),
    ABSENCE_NOON("absenceNoon"),

    WAITING_VACATION_FULL("waitingVacationFull"),
    WAITING_VACATION_MORNING("waitingVacationMorning"),
    WAITING_VACATION_NOON("waitingVacationNoon"),

    ALLOWED_VACATION_FULL("allowedVacationFull"),
    ALLOWED_VACATION_MORNING("allowedVacationMorning"),
    ALLOWED_VACATION_NOON("allowedVacationNoon"),

    ACTIVE_SICKNOTE_FULL("activeSickNoteFull"),
    ACTIVE_SICKNOTE_MORNING("activeSickNoteMorning"),
    ACTIVE_SICKNOTE_NOON("activeSickNoteNoon"),

    PUBLIC_HOLIDAY_MORNING("publicHolidayMorning"),
    PUBLIC_HOLIDAY_NOON("publicHolidayNoon"),
    PUBLIC_HOLIDAY_FULL("publicHolidayFull"),
    ;

    private final String identifier;

    AbsenceOverviewDayType(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
