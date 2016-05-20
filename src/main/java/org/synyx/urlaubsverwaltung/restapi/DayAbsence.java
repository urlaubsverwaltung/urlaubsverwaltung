package org.synyx.urlaubsverwaltung.restapi;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.core.period.DayLength;

import java.math.BigDecimal;


/**
 * Represents an absence for a day.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
class DayAbsence {

    enum Type {

        VACATION,
        SICK_NOTE
    }

    private final String date;
    private final BigDecimal dayLength;
    private final String type;
    private final String status;
    private final String href;

    DayAbsence(DateMidnight date, DayLength dayLength, DayAbsence.Type type, String status, Integer id) {

        this.date = date.toString(RestApiDateFormat.PATTERN);
        this.dayLength = dayLength.getDuration();
        this.type = type.name();
        this.status = status;
        this.href = id.toString();
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
