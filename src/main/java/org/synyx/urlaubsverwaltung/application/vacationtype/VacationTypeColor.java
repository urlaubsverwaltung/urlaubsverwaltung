package org.synyx.urlaubsverwaltung.application.vacationtype;

public enum VacationTypeColor {
    GRAY,
    ORANGE,
    YELLOW,
    EMERALD,
    CYAN,
    BLUE,
    VIOLET,
    PINK;

    public static VacationTypeColor defaultColor() {
        return YELLOW;
    }
}
