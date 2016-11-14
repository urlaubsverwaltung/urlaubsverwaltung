package org.synyx.urlaubsverwaltung.restapi.availability;

import java.math.BigDecimal;


/**
 * Represents the availability for a person on a given day. Also contains the reason for being absent.
 *
 * @author  Marc Kannegiesser - kannegiesser@synyx.de
 * @author  Timo Eifler - eifler@synyx.de
 */
class DayAvailability {

    private final String date;
    private final BigDecimal availabilityRatio;
    private final TimedAbsenceSpans timedAbsenceSpans;

    public DayAvailability(BigDecimal availabilityRatio, String date, TimedAbsenceSpans timedAbsenceSpans) {

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
