package org.synyx.urlaubsverwaltung.restapi.availability;

import org.synyx.urlaubsverwaltung.core.period.DayLength;

import java.math.BigDecimal;


/**
 * Details for a (partial) absence of a person on a day.
 *
 * @author  Timo Eifler - eifler@synyx.de
 */

class TimedAbsence {

    enum Type {

        VACATION,
        SICK_NOTE,
        WORK,
        FREETIME,
        HOLIDAY
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
