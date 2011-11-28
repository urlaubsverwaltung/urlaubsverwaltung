package org.synyx.urlaubsverwaltung.domain;

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina
 */
public class LeaveDay {

    private enum Length {

        FULL("full"),
        MORNING("half.morning"),
        NOON("half.noon");

        private String dayLength;

        private Length(String dayLength) {

            this.dayLength = dayLength;
        }

        public String getDayLength() {

            return this.dayLength;
        }
    }

    private BigDecimal days;

    public BigDecimal getDays() {

        return days;
    }


    public void setDays(BigDecimal days) {

        this.days = days;
    }
}
