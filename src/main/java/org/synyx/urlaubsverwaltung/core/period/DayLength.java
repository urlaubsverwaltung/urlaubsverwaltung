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
}
