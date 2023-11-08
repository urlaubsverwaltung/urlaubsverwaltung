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

    public enum GenericAbsenceType {
        VACATION,
        SICK_NOTE,
        NO_WORKDAY,
        PUBLIC_HOLIDAY
    }

    private final String date;
    private final GenericAbsenceType genericType;
    private final Long id;
    private final String status;
    private final String absent;
    private final double absentNumeric;
    private final String typeCategory;
    private final Long typeId;

    AbsenceDto(LocalDate date, GenericAbsenceType genericType, String status, DayLength dayLength, String typeCategory, Long typeId) {
        this(date, genericType, null, status, dayLength, typeCategory, typeId);
    }

    AbsenceDto(LocalDate date, GenericAbsenceType genericType, Long id, String status, DayLength dayLength, String typeCategory, Long typeId) {
        this.date = date.format(ofPattern(DATE_PATTERN));
        this.genericType = genericType;
        this.id = id;
        this.status = status;
        this.absent = dayLength.name();
        this.absentNumeric = dayLength.getDuration().doubleValue();
        this.typeCategory = typeCategory;
        this.typeId = typeId;
    }

    public String getDate() {
        return date;
    }

    public GenericAbsenceType getGenericType() {
        return genericType;
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

    public String getTypeCategory() {
        return typeCategory;
    }

    public Long getTypeId() {
        return typeId;
    }
}
