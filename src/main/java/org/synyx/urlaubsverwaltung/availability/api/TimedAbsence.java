package org.synyx.urlaubsverwaltung.availability.api;

import org.synyx.urlaubsverwaltung.period.DayLength;

import java.math.BigDecimal;


/**
 * Details for a (partial) absence of a person on a day.
 */
class TimedAbsence {

    enum Type {

        VACATION,
        SICK_NOTE,
        WORK,
        FREETIME,
        PUBLIC_HOLIDAY
    }

    private final Type type;
    private final BigDecimal ratio;
    private final String partOfDay;

    public TimedAbsence(DayLength dayLength, Type type) {

        this.type = type;
        this.ratio = dayLength.getDuration();
        this.partOfDay = dayLength.name();
    }

    public Type getType() {

        return type;
    }


    public BigDecimal getRatio() {

        return ratio;
    }


    public String getPartOfDay() {

        return partOfDay;
    }
}
