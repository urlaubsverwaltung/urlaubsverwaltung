package org.synyx.urlaubsverwaltung.availability.api;

import org.synyx.urlaubsverwaltung.period.DayLength;

import java.math.BigDecimal;


/**
 * Details for a (partial) absence of a person on a day.
 */
@Deprecated(forRemoval = true, since = "4.4.0")
class TimedAbsence {

    private final BigDecimal ratio;
    private final String partOfDay;

    public TimedAbsence(DayLength dayLength) {

        this.ratio = dayLength.getDuration();
        this.partOfDay = dayLength.name();
    }

    public BigDecimal getRatio() {

        return ratio;
    }


    public String getPartOfDay() {

        return partOfDay;
    }
}
