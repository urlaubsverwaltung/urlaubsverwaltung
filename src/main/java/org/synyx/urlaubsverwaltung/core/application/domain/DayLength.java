package org.synyx.urlaubsverwaltung.core.application.domain;

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina
 */
public enum DayLength {

    FULL(BigDecimal.valueOf(1.0)),
    MORNING(BigDecimal.valueOf(0.5)),
    NOON(BigDecimal.valueOf(0.5)),
    ZERO(BigDecimal.ZERO);

    private BigDecimal duration;

    private DayLength(BigDecimal duration) {

        this.duration = duration;
    }

    public BigDecimal getDuration() {

        return this.duration;
    }
}
