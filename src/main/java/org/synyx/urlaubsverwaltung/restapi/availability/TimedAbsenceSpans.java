package org.synyx.urlaubsverwaltung.restapi.availability;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;


/**
 * Wrapper class for a list of {@link TimedAbsence} which provides additional utility methods to retrieve information
 * about the list.
 *
 * @author  Timo Eifler - eifler@synyx.de
 */
class TimedAbsenceSpans {

    private List<TimedAbsence> absencesList;

    TimedAbsenceSpans(List<TimedAbsence> absencesList) {

        if (absencesList == null) {
            this.absencesList = new ArrayList<>();
        } else {
            this.absencesList = absencesList;
        }
    }

    public List<TimedAbsence> getAbsencesList() {

        return absencesList;
    }


    BigDecimal calculatePresenceRatio() {

        BigDecimal presenceRatio = BigDecimal.ONE;

        for (TimedAbsence absenceSpan : absencesList) {
            presenceRatio = presenceRatio.subtract(absenceSpan.getRatio());
        }

        boolean negativePresenceRatio = presenceRatio.compareTo(BigDecimal.ZERO) < 0;

        if (negativePresenceRatio) {
            presenceRatio = BigDecimal.ZERO;
        }

        return presenceRatio;
    }
}
