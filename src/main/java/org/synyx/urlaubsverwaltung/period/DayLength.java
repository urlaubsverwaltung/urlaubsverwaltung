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
        return switch (this) {
            case FULL -> ZERO;
            case MORNING -> NOON;
            case NOON -> MORNING;
            case ZERO -> FULL;
        };
    }

    public boolean isHalfDay() {
        return isHalfDay;
    }

    public boolean isMorning() {
        return MORNING.equals(this);
    }

    public boolean isNoon() {
        return NOON.equals(this);
    }

    public boolean isFull() {
        return FULL.equals(this);
    }
}
