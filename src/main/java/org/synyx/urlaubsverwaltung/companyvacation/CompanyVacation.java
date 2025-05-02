package org.synyx.urlaubsverwaltung.companyvacation;

import org.synyx.urlaubsverwaltung.period.DayLength;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;

/**
 * Describes a company vacation with the UV specific setting if it is a half public holiday or not.
 * (e.g. Christmas Eve can be configured to be a NOON public holiday for instance.)
 */
public record CompanyVacation(LocalDate date, DayLength dayLength, String description) {

    public boolean isMorning() {
        return dayLength.equals(MORNING);
    }

    public boolean isNoon() {
        return dayLength.equals(NOON);
    }

    public boolean isFull() {
        return dayLength.equals(FULL);
    }

    public BigDecimal workingDuration() {
        return dayLength.getInverse().getDuration();
    }
}
