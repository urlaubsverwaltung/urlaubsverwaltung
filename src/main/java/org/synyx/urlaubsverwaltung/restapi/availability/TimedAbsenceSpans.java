package org.synyx.urlaubsverwaltung.restapi.availability;

import java.math.BigDecimal;

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

        this.absencesList = absencesList;
    }

    List<TimedAbsence> getAbsencesList() {

        return absencesList;
    }


    BigDecimal calculatePresenceRatio() {

        BigDecimal absenceRatio = BigDecimal.ZERO;

        for (TimedAbsence absenceSpan : absencesList) {
            absenceRatio = absenceRatio.add(absenceSpan.getRatio());
        }

        BigDecimal presenceRatio = BigDecimal.ONE.subtract(absenceRatio);

        boolean negativePresenceRatio = presenceRatio.compareTo(BigDecimal.ZERO) < 0;

        if (negativePresenceRatio) {
            presenceRatio = BigDecimal.ZERO;
        }

        return presenceRatio;
    }
}
