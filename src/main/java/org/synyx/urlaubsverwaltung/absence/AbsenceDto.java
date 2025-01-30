package org.synyx.urlaubsverwaltung.absence;

import org.springframework.hateoas.RepresentationModel;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.synyx.urlaubsverwaltung.absence.AbsenceDto.AbsenceType.NO_WORKDAY;
import static org.synyx.urlaubsverwaltung.absence.AbsenceDto.AbsenceType.PUBLIC_HOLIDAY;
import static org.synyx.urlaubsverwaltung.absence.AbsenceDto.AbsenceType.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.absence.AbsenceDto.AbsenceType.VACATION;
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

    AbsenceDto(LocalDate date, DayLength dayLength, AbsencePeriod.RecordInfo recordInfo) {
        this.date = date.format(ofPattern(DATE_PATTERN));
        this.absenceType = toAbsenceTypes(recordInfo.getAbsenceType());
        this.id = recordInfo.getId().orElse(null);
        this.status = recordInfo.getStatus().name();
        this.absent = dayLength.name();
        this.absentNumeric = dayLength.getDuration().doubleValue();
        this.category = recordInfo.getCategory().orElse(null);
        this.typeId = recordInfo.getTypeId().orElse(null);

        if (VacationCategory.OVERTIME.name().equals(category) && id != null) {
            this.add(linkTo(methodOn(OvertimeAbsenceApiController.class).overtimeAbsence(recordInfo.getPerson().getId(), id)).withRel("overtime"));
        }
    }

    private AbsenceDto.AbsenceType toAbsenceTypes(AbsencePeriod.AbsenceType genericAbsenceType) {
        return switch (genericAbsenceType) {
            case VACATION -> VACATION;
            case SICK -> SICK_NOTE;
            case NO_WORKDAY -> NO_WORKDAY;
            case PUBLIC_HOLIDAY -> PUBLIC_HOLIDAY;
        };
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
