package org.synyx.urlaubsverwaltung.core.period;

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina
 */
public enum DayLength {

    FULL(new BigDecimal("1.0")),
    MORNING(new BigDecimal("0.5")),
    NOON(new BigDecimal("0.5")),
    ZERO(BigDecimal.ZERO);

    private BigDecimal duration;

    DayLength(BigDecimal duration) {

        this.duration = duration;
    }

    public BigDecimal getDuration() {

        return this.duration;
    }


    /**
     * Pairs of enum members can be added to get a full day.
     *
     * @return  the matching enum member which represents the rest of the day.
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
}
