package org.synyx.urlaubsverwaltung.overview;

import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static java.math.BigDecimal.ZERO;

/**
 * Represents number of days for specific application states.
 */
public class UsedDays {

    private final Map<String, BigDecimal> days;

    UsedDays(ApplicationStatus... status) {

        days = new HashMap<>();

        for (ApplicationStatus applicationStatus : status) {
            days.put(applicationStatus.name(), ZERO);
        }
    }

    public Map<String, BigDecimal> getDays() {
        return days;
    }

    public BigDecimal getSum() {
        return days.values().stream().reduce(ZERO, BigDecimal::add);
    }

    void addDays(ApplicationStatus status, BigDecimal days) {

        final String statusAsString = status.name();

        if (!this.days.containsKey(statusAsString)) {
            // this status has not been used in initialization, so it's not supported here
            throw new UnsupportedOperationException("Application status " + statusAsString + " not allowed here");
        }

        final BigDecimal addedDays = this.days.get(statusAsString).add(days);
        this.days.put(statusAsString, addedDays);
    }
}
