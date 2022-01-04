package org.synyx.urlaubsverwaltung.period;

import java.math.BigDecimal;

public enum DayLength {

    FULL(BigDecimal.ONE, false),
    MORNING(new BigDecimal("0.5"), true),
    NOON(new BigDecimal("0.5"), true),
    ZERO(BigDecimal.ZERO, false);

    private final BigDecimal duration;
    private final boolean isHalfDay;

    DayLength(BigDecimal duration, boolean isHalfDay) {
        this.duration = duration;
        this.isHalfDay = isHalfDay;
    }

    public BigDecimal getDuration() {
        return this.duration;
    }

    /**
     * Pairs of enum members can be added to get a full day.
     *
     * @return the matching enum member which represents the rest of the day.
     */
    public DayLength getInverse() {
        switch (this) {
            case FULL:
                return ZERO;
            case MORNING:
                return NOON;
            case NOON:
                return MORNING;
            case ZERO:
                return FULL;
        }

        // this is not relevant, because every case is defined in the switch case, this will be nicer to read and
        // without this last return case in java 17
        return ZERO;
    }

    public boolean isHalfDay() { return isHalfDay; }
}
