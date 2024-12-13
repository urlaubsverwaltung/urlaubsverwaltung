package org.synyx.urlaubsverwaltung.extension.backup.model;


import org.synyx.urlaubsverwaltung.period.DayLength;

public enum DayLengthDTO {
    FULL,
    MORNING,
    NOON,
    ZERO;

    public DayLength toDayLength() {
        return DayLength.valueOf(this.name());
    }
}
