package org.synyx.urlaubsverwaltung.application.domain;

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina
 */
public enum DayLength {

    FULL("full", BigDecimal.valueOf(1.0)),
    MORNING("half.morning", BigDecimal.valueOf(0.5)),
    NOON("half.noon", BigDecimal.valueOf(0.5));

    private String dayLength;
    private BigDecimal dayLengthNumber;

    private DayLength(String dayLength, BigDecimal dayLengthNumber) {

        this.dayLength = dayLength;
        this.dayLengthNumber = dayLengthNumber;
    }

    public String getDayLength() {

        return this.dayLength;
    }


    public BigDecimal getDayLengthNumber() {

        return this.dayLengthNumber;
    }
}
