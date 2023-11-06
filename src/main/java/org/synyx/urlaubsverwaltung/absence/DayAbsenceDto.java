package org.synyx.urlaubsverwaltung.absence;

import org.synyx.urlaubsverwaltung.api.RestApiDateFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents an absence for a day.
 */
public class DayAbsenceDto {

    public enum Type {
        VACATION,
        SICK_NOTE,
        NO_WORKDAY
    }

    private final String date;
    private final BigDecimal dayLength;
    private final String absencePeriodName;
    private final String type;
    private final String status;
    private final String href;
    private final Long vacationTypeId;

    DayAbsenceDto(LocalDate date, BigDecimal dayLength, String absencePeriodName, String type, String status, Long id) {
        this(date, dayLength, absencePeriodName, type, status, id, null);
    }

    DayAbsenceDto(LocalDate date, BigDecimal dayLength, String absencePeriodName, String type, String status, Long id, Long vacationTypeId) {
        this.date = date.format(DateTimeFormatter.ofPattern(RestApiDateFormat.DATE_PATTERN));
        this.dayLength = dayLength;
        this.absencePeriodName = absencePeriodName;
        this.type = type;
        this.status = status;
        this.href = id == null ? "" : id.toString();
        this.vacationTypeId = vacationTypeId;
    }

    public String getDate() {
        return date;
    }

    public BigDecimal getDayLength() {
        return dayLength;
    }

    public String getAbsencePeriodName() {
        return absencePeriodName;
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

    public Long getVacationTypeId() {
        return vacationTypeId;
    }

    @Override
    public String toString() {
        return "DayAbsenceDto{" +
            "date='" + date + '\'' +
            ", dayLength=" + dayLength +
            ", absencePeriodName='" + absencePeriodName + '\'' +
            ", type='" + type + '\'' +
            ", status='" + status + '\'' +
            ", href='" + href + '\'' +
            ", vacationTypeId=" + vacationTypeId +
            '}';
    }
}
