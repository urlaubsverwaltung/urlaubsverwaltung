package org.synyx.urlaubsverwaltung.restapi;

import org.synyx.urlaubsverwaltung.core.period.DayLength;

import java.math.BigDecimal;

import java.util.List;


/**
 * Represents the availability for a person on a given day. Also contains the reason for being absent.
 *
 * @author  Marc Kannegiesser - kannegiesser@synyx.de
 * @author  Timo Eifler - eifler@synyx.de
 */
class DayAvailability {

    static class TimedAbsence {

        enum Type {

            VACATION,
            SICK_NOTE,
            WORK,
            FREETIME,
            HOLIDAY
        }

        private final Type type;
        private final BigDecimal availabilityRatio;
        private final String partOfDay;

        public TimedAbsence(DayLength dayLength, Type type) {

            this.type = type;
            this.availabilityRatio = dayLength.getDuration();
            this.partOfDay = dayLength.name();
        }

        public Type getType() {

            return type;
        }


        public BigDecimal getAvailabilityRatio() {

            return availabilityRatio;
        }


        public String getPartOfDay() {

            return partOfDay;
        }
    }

    private final String date;
    private final BigDecimal availabilityRatio;
    private final List<TimedAbsence> spans;

    public DayAvailability(BigDecimal availabilityRatio, String date, List<TimedAbsence> spans) {

        this.availabilityRatio = availabilityRatio;
        this.date = date;
        this.spans = spans;
    }

    public String getDate() {

        return date;
    }


    public List<TimedAbsence> getAbsenceSpans() {

        return spans;
    }


    public BigDecimal getAvailabilityRatio() {

        return availabilityRatio;
    }
}
