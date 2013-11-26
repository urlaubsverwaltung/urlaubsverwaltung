package org.synyx.urlaubsverwaltung.application.domain;

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina
 */
public enum DayLength {

    FULL("full", BigDecimal.valueOf(1.0)),
    MORNING("half.morning", BigDecimal.valueOf(0.5)),
    NOON("half.noon", BigDecimal.valueOf(0.5)),
    ZERO("zero", BigDecimal.ZERO);

    private String dayLength;
    private BigDecimal duration;

    private DayLength(String dayLength, BigDecimal duration) {

        this.dayLength = dayLength;
        this.duration = duration;
    }

    public String getDayLength() {

        return this.dayLength;
    }


    public BigDecimal getDuration() {

        return this.duration;
    }
}
