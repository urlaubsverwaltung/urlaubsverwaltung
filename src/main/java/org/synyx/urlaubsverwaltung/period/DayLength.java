package org.synyx.urlaubsverwaltung.period;

import java.math.BigDecimal;

public enum DayLength {

    FULL(BigDecimal.ONE),
    MORNING(new BigDecimal("0.5")),
    NOON(new BigDecimal("0.5")),
    ZERO(BigDecimal.ZERO);

    private final BigDecimal duration;

    DayLength(BigDecimal duration) {
        this.duration = duration;
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

        return null;
    }

    public boolean isHalfDay() { return duration.equals(MORNING.duration); }
}
