package org.synyx.urlaubsverwaltung.absence;

import org.springframework.hateoas.RepresentationModel;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.synyx.urlaubsverwaltung.api.RestApiDateFormat.DATE_PATTERN;

/**
 * Represents an absence for a day.
 */
public class AbsenceDto extends RepresentationModel<AbsenceDto> {

    public enum AbsenceType {
        VACATION,
        SICK_NOTE,
        NO_WORKDAY,
        PUBLIC_HOLIDAY
    }

    private final String date;
    private final AbsenceType absenceType;
    private final Long id;
    private final String status;
    private final String absent;
    private final double absentNumeric;
    private final String category;
    private final Long typeId;

    AbsenceDto(LocalDate date, AbsenceType absenceType, String status, DayLength dayLength, String category, Long typeId) {
        this(date, absenceType, null, status, dayLength, category, typeId);
    }

    AbsenceDto(LocalDate date, AbsenceType absenceType, Long id, String status, DayLength dayLength, String category, Long typeId) {
        this.date = date.format(ofPattern(DATE_PATTERN));
        this.absenceType = absenceType;
        this.id = id;
        this.status = status;
        this.absent = dayLength.name();
        this.absentNumeric = dayLength.getDuration().doubleValue();
        this.category = category;
        this.typeId = typeId;
    }

    public String getDate() {
        return date;
    }

    public AbsenceType getAbsenceType() {
        return absenceType;
    }

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getAbsent() {
        return absent;
    }

    public double getAbsentNumeric() {
        return absentNumeric;
    }

    public String getCategory() {
        return category;
    }

    public Long getTypeId() {
        return typeId;
    }
}
