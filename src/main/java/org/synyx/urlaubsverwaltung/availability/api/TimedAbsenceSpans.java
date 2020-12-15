package org.synyx.urlaubsverwaltung.availability.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.math.BigDecimal.ZERO;


/**
 * Wrapper class for a list of {@link TimedAbsence} which provides additional utility methods to retrieve information
 * about the list.
 */
@Deprecated(forRemoval = true, since = "4.4.0")
class TimedAbsenceSpans {

    private final List<TimedAbsence> absencesList;

    TimedAbsenceSpans(List<TimedAbsence> absencesList) {
        this.absencesList = Objects.requireNonNullElseGet(absencesList, ArrayList::new);
    }

    public List<TimedAbsence> getAbsencesList() {
        return absencesList;
    }

    BigDecimal calculatePresenceRatio() {

        BigDecimal presenceRatio = BigDecimal.ONE;

        for (TimedAbsence absenceSpan : absencesList) {
            presenceRatio = presenceRatio.subtract(absenceSpan.getRatio());
        }

        final boolean negativePresenceRatio = presenceRatio.compareTo(ZERO) < 0;
        if (negativePresenceRatio) {
            presenceRatio = ZERO;
        }

        return presenceRatio;
    }
}
