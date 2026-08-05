package org.synyx.urlaubsverwaltung.overtime.statistics;

import java.time.Duration;

import static java.time.Duration.ZERO;

/**
 * Company wide overtime figures over the whole history, without any reference to a year.
 *
 * <p>
 * The balance answers "how much overtime does the company have open right now" and is by construction the same figure
 * every person sees as their own remaining overtime, summed up.
 *
 * @param accrued   accrued overtime over the whole history, never negative
 * @param reduction reduced overtime over the whole history, given as a positive amount
 */
record OvertimeTotals(Duration accrued, Duration reduction) {

    static OvertimeTotals empty() {
        return new OvertimeTotals(ZERO, ZERO);
    }

    Duration balance() {
        return accrued.minus(reduction);
    }
}
