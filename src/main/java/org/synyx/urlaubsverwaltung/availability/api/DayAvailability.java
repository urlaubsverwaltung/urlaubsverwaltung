package org.synyx.urlaubsverwaltung.availability.api;

import java.math.BigDecimal;


/**
 * Represents the availability for a person on a given day. Also contains the reason for being absent.
 */
class DayAvailability {

    private final String date;
    private final BigDecimal availabilityRatio;
    private final TimedAbsenceSpans timedAbsenceSpans;

    DayAvailability(BigDecimal availabilityRatio, String date, TimedAbsenceSpans timedAbsenceSpans) {

        this.availabilityRatio = availabilityRatio;
        this.date = date;
        this.timedAbsenceSpans = timedAbsenceSpans;
    }

    public String getDate() {

        return date;
    }


    public TimedAbsenceSpans getTimedAbsenceSpans() {

        return timedAbsenceSpans;
    }


    public BigDecimal getAvailabilityRatio() {

        return availabilityRatio;
    }
}
