package org.synyx.urlaubsverwaltung.restapi;

import java.math.BigDecimal;
import java.util.List;


/**
 * Represents an absence for a day.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
class DayAvailability {
    

    static class TimedAbsence extends Absence {
        private final BigDecimal hours;

        public TimedAbsence(BigDecimal hours, Type type) {
            super(type);
            this.hours = hours;
        }

        public BigDecimal getHours() {
            return hours;
        }
    }
    
    static class Absence {
        enum Type {
            VACATION,
            SICK_NOTE,
            WORK, 
            FREETIME, 
            HOLIDAY
        }
        
        private final Type type;

        public Absence(Type type) {
            this.type = type;
        }
        
        public Type getType() {
            return type;
        }
    }
    
   
    private final String date;
    private final BigDecimal hoursAvailable;
    private final List<Absence> spans;

    public DayAvailability(BigDecimal hoursAvailable, String date, List<Absence> spans) {
        this.hoursAvailable = hoursAvailable;
        this.date = date;
        this.spans = spans;
    }

    public String getDate() {

        return date;
    }

    public List<Absence> getAbsenceSpans() {
        return spans;
    }

    public BigDecimal getHoursAvailable() {
        return hoursAvailable;
    }
}
