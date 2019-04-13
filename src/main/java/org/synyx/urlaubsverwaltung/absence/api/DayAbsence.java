package org.synyx.urlaubsverwaltung.absence.api;

import org.synyx.urlaubsverwaltung.api.RestApiDateFormat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


/**
 * Represents an absence for a day.
 */
public class DayAbsence {

    public enum Type {

        VACATION,
        SICK_NOTE
    }

    private final String date;
    private final String dayLength;
    private final String type;
    private final String status;
    private final String href;

    DayAbsence(LocalDate date, String dayLength, Type type, String status, Integer id) {

        this.date = date.format(DateTimeFormatter.ofPattern(RestApiDateFormat.DATE_PATTERN));
        this.dayLength = dayLength;
        this.type = type.name();
        this.status = status;
        this.href = id == null ? "" : id.toString();
    }

    public String getDate() {

        return date;
    }


    public String getDayLength() {

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
