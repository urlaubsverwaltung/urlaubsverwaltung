package org.synyx.urlaubsverwaltung.statistics.web;

import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


/**
 * Represents number of days for specific application states.
 */
public class UsedDays {

    private final Map<String, BigDecimal> days;

    public UsedDays(ApplicationStatus... status) {

        days = new HashMap<>();

        for (ApplicationStatus applicationStatus : status) {
            days.put(applicationStatus.name(), BigDecimal.ZERO);
        }
    }

    public Map<String, BigDecimal> getDays() {

        return days;
    }


    public void addDays(ApplicationStatus status, BigDecimal days) {

        String statusAsString = status.name();

        if (!this.days.containsKey(statusAsString)) {
            // this status has not been used in initialization, so it's not supported here
            throw new UnsupportedOperationException("Application status " + statusAsString + " not allowed here");
        }

        BigDecimal addedDays = this.days.get(statusAsString).add(days);
        this.days.put(statusAsString, addedDays);
    }
}
