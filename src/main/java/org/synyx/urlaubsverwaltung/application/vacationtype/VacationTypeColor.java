package org.synyx.urlaubsverwaltung.application.vacationtype;

public enum VacationTypeColor {
    NEUTRAL,
    RED, // TODO -> no red yet, used by sick-note (hard coded currently)
    ORANGE,
    YELLOW,
    LIME,
    CYAN,
    BLUE,
    VIOLET,
    FUCHSIA;

    public static VacationTypeColor defaultColor() {
        return YELLOW;
    }
}
