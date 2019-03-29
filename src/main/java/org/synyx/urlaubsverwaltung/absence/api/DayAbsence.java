package org.synyx.urlaubsverwaltung.absence.api;

import org.joda.time.DateMidnight;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.api.RestApiDateFormat;

import java.math.BigDecimal;


/**
 * Represents an absence for a day.
 */
public class DayAbsence {

    public enum Type {

        VACATION,
        SICK_NOTE
    }

    private final String date;
    private final BigDecimal dayLength;
    private final String type;
    private final String status;
    private final String href;

    public DayAbsence(DateMidnight date, DayLength dayLength, DayAbsence.Type type, String status, Integer id) {

        this.date = date.toString(RestApiDateFormat.DATE_PATTERN);
        this.dayLength = dayLength.getDuration();
        this.type = type.name();
        this.status = status;
        this.href = id == null ? "" : id.toString();
    }

    public String getDate() {

        return date;
    }


    public BigDecimal getDayLength() {

        return dayLength;
    }


    public String getType() {

        return type;
    }


    public String getStatus() {

        return status;
    }


    public String getHref() {

        return href;
    }
}
